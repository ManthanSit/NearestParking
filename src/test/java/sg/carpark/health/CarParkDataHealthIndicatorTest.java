package sg.carpark.health;

import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Status;
import sg.carpark.availability.AvailabilityRepository;
import sg.carpark.availability.AvailabilitySnapshot;
import sg.carpark.catalog.CarParkCandidate;
import sg.carpark.catalog.CarParkCatalogRepository;
import sg.carpark.domain.CarPark;
import sg.carpark.domain.Wgs84Coordinate;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CarParkDataHealthIndicatorTest {
    private static final Instant NOW = Instant.parse("2026-10-03T12:00:00Z");
    private static final Duration MAX_AGE = Duration.ofMinutes(2);

    @Test
    void isUpWhenCatalogAndAvailabilityTimestampAreFresh() {
        var indicator = indicator(true, NOW.minusSeconds(119));

        assertThat(indicator.health().getStatus()).isEqualTo(Status.UP);
        assertThat(indicator.health().getDetails()).containsEntry("availability", "fresh");
    }

    @Test
    void reportsStaleAvailabilityAndIsDownWhenSourceTimestampIsTooOld() {
        var indicator = indicator(true, NOW.minus(MAX_AGE).minusMillis(1));

        assertThat(indicator.health().getStatus()).isEqualTo(Status.DOWN);
        assertThat(indicator.health().getDetails()).containsEntry("availability", "stale");
    }

    @Test
    void isDownWhenAvailabilityHasNoSnapshot() {
        var indicator = indicator(false, null);

        assertThat(indicator.health().getStatus()).isEqualTo(Status.DOWN);
        assertThat(indicator.health().getDetails()).containsEntry("availability", "missing");
    }

    private static CarParkDataHealthIndicator indicator(boolean withSnapshot, Instant sourceTimestamp) {
        CarParkCatalogRepository catalogue = new TestCatalogue();
        AvailabilityRepository availability = new TestAvailability(withSnapshot
                ? new AvailabilitySnapshot(sourceTimestamp, java.util.Map.of()) : null);
        return new CarParkDataHealthIndicator(catalogue, availability,
                Clock.fixed(NOW, ZoneOffset.UTC), MAX_AGE);
    }

    private static final class TestCatalogue implements CarParkCatalogRepository {
        public boolean hasCatalogue() { return true; }
        public List<CarPark> getCatalogue() { return List.of(); }
        public List<CarParkCandidate> findWithin(Wgs84Coordinate origin, double radiusKm) { return List.of(); }
        public void replaceCatalogue(List<CarPark> carParks) { }
        public long acquireRefreshCooldown(Duration cooldown) { return 0; }
    }

    private static final class TestAvailability implements AvailabilityRepository {
        private final AvailabilitySnapshot snapshot;
        private TestAvailability(AvailabilitySnapshot snapshot) { this.snapshot = snapshot; }
        public void publish(AvailabilitySnapshot snapshot) { }
        public boolean hasSnapshot() { return snapshot != null; }
        public AvailabilitySnapshot getSnapshot() { return snapshot; }
    }
}
