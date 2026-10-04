package sg.carpark.catalog;

import sg.carpark.domain.CarPark;
import sg.carpark.domain.Wgs84Coordinate;

import java.time.Duration;
import java.util.List;

public interface CarParkCatalogRepository {
    boolean hasCatalogue();

    List<CarPark> getCatalogue();

    List<CarParkCandidate> findWithin(Wgs84Coordinate origin, double radiusKm);

    void replaceCatalogue(List<CarPark> carParks);

    long acquireRefreshCooldown(Duration cooldown);
}
