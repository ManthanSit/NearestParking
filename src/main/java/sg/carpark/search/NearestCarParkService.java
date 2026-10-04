package sg.carpark.search;

import org.springframework.stereotype.Service;
import sg.carpark.availability.AvailabilityRepository;
import sg.carpark.availability.AvailabilitySnapshot;
import sg.carpark.catalog.CarParkCatalogRepository;
import sg.carpark.domain.VehicleType;
import sg.carpark.domain.Wgs84Coordinate;

@Service
public class NearestCarParkService {
    private final CarParkCatalogRepository catalogue;
    private final AvailabilityRepository availability;
    private final NearestCarParkSelector selector = new NearestCarParkSelector();

    public NearestCarParkService(CarParkCatalogRepository catalogue, AvailabilityRepository availability) {
        this.catalogue = catalogue;
        this.availability = availability;
    }

    public SearchResponse search(double latitude, double longitude, VehicleType vehicleType, double radiusKm, int limit) {
        AvailabilitySnapshot snapshot = availability.getSnapshot();
        if (!catalogue.hasCatalogue() || snapshot == null) throw new DataNotReadyException();
        var results = selector.select(vehicleType, limit,
                catalogue.findWithin(new Wgs84Coordinate(longitude, latitude), radiusKm), snapshot.byCarPark());
        return new SearchResponse(radiusKm, limit, snapshot.sourceTimestamp(), results);
    }

    public record SearchResponse(double radiusKm, int limit, java.time.Instant availabilityUpdatedAt,
                                 java.util.List<CarParkSearchResult> results) {}

    public static class DataNotReadyException extends RuntimeException {
        public DataNotReadyException() { super("Car park data is not ready"); }
    }
}
