package sg.carpark.health;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import sg.carpark.availability.AvailabilityRepository;
import sg.carpark.catalog.CarParkCatalogRepository;

@Component("carParkData")
public class CarParkDataHealthIndicator implements HealthIndicator {
    private final CarParkCatalogRepository catalogue;
    private final AvailabilityRepository availability;
    private final Clock clock;
    private final Duration maxAvailabilityAge;

    @Autowired
    public CarParkDataHealthIndicator(CarParkCatalogRepository catalogue, AvailabilityRepository availability,
                                      Environment environment) {
        this(catalogue, availability, Clock.systemUTC(), environment.getProperty(
                "carpark.availability.stale-after", Duration.class, Duration.ofMinutes(2)));
    }

    CarParkDataHealthIndicator(CarParkCatalogRepository catalogue, AvailabilityRepository availability,
                               Clock clock, Duration maxAvailabilityAge) {
        this.catalogue = catalogue;
        this.availability = availability;
        this.clock = clock;
        this.maxAvailabilityAge = maxAvailabilityAge;
    }

    @Override
    public Health health() {
        boolean catalogueReady = catalogue.hasCatalogue();
        var snapshot = availability.getSnapshot();
        if (snapshot == null) {
            return Health.down().withDetail("catalogue", catalogueReady ? "ready" : "missing")
                    .withDetail("availability", "missing").build();
        }
        Instant sourceTimestamp = snapshot.sourceTimestamp();
        Duration age = Duration.between(sourceTimestamp, clock.instant());
        boolean availabilityFresh = age.compareTo(maxAvailabilityAge) <= 0;
        Health.Builder health = catalogueReady && availabilityFresh ? Health.up() : Health.down();
        return health.withDetail("catalogue", catalogueReady ? "ready" : "missing")
                .withDetail("availability", availabilityFresh ? "fresh" : "stale")
                .withDetail("availabilityUpdatedAt", sourceTimestamp)
                .withDetail("availabilityAgeSeconds", Math.max(0, age.getSeconds())).build();
    }
}
