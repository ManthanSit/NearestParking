package sg.carpark.catalog;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import sg.carpark.domain.CarPark;
import sg.carpark.domain.Wgs84Coordinate;
import sg.carpark.geo.Svy21CoordinateConverter;

import java.time.Duration;
import java.util.List;

@Service
public class CarParkCatalogService {
    private final CarParkCatalogRepository repository;
    private final CarParkCatalogSource source;
    private final Svy21CoordinateConverter converter;
    private final Duration refreshCooldown;

    @Autowired
    public CarParkCatalogService(
            CarParkCatalogRepository repository,
            CarParkCatalogSource source,
            Svy21CoordinateConverter converter,
            org.springframework.core.env.Environment environment) {
        this(repository, source, converter,
                Duration.ofSeconds(environment.getProperty("carpark.catalog.refresh-cooldown-seconds", Long.class, 600L)));
    }

    CarParkCatalogService(CarParkCatalogRepository repository, CarParkCatalogSource source,
                          Svy21CoordinateConverter converter, Duration refreshCooldown) {
        this.repository = repository;
        this.source = source;
        this.converter = converter;
        this.refreshCooldown = refreshCooldown;
    }

    public int ensureLoaded() {
        if (repository.hasCatalogue()) {
            return repository.getCatalogue().size();
        }
        return fetchAndPublish();
    }

    public int refreshManually() {
        long retryAfter = repository.acquireRefreshCooldown(refreshCooldown);
        if (retryAfter > 0) {
            throw new CatalogRefreshCooldownException(retryAfter);
        }
        return fetchAndPublish();
    }

    private int fetchAndPublish() {
        try {
            List<SourceCarPark> sourceCarParks = source.fetchCatalogue();
            if (sourceCarParks == null || sourceCarParks.isEmpty()) {
                throw new IllegalStateException("HDB catalogue response contained no car parks");
            }
            List<CarPark> carParks = sourceCarParks.stream()
                    .filter(item -> item.carparkNumber() != null && !item.carparkNumber().isBlank())
                    .map(item -> {
                        Wgs84Coordinate coordinate = converter.toWgs84(item.easting(), item.northing());
                        return new CarPark(item.carparkNumber(), item.address(), coordinate.longitude(), coordinate.latitude());
                    })
                    .toList();
            if (carParks.isEmpty()) {
                throw new IllegalStateException("HDB catalogue response contained no valid car parks");
            }
            repository.replaceCatalogue(carParks);
            return carParks.size();
        } catch (RuntimeException exception) {
            if (exception instanceof CatalogRefreshFailedException failed) throw failed;
            throw new CatalogRefreshFailedException("HDB catalogue refresh failed", exception);
        }
    }
}
