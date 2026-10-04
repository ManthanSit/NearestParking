package sg.carpark.search;

import sg.carpark.catalog.CarParkCandidate;
import sg.carpark.domain.LotAvailability;
import sg.carpark.domain.VehicleType;

import java.util.List;
import java.util.Map;

public class NearestCarParkSelector {
    public List<CarParkSearchResult> select(
            VehicleType vehicleType,
            int limit,
            List<CarParkCandidate> candidates,
            Map<String, List<LotAvailability>> availability) {
        return candidates.stream()
                .map(candidate -> toResult(vehicleType, candidate, availability.get(candidate.carPark().carparkNumber())))
                .flatMap(java.util.Optional::stream)
                .limit(limit)
                .toList();
    }

    private java.util.Optional<CarParkSearchResult> toResult(
            VehicleType vehicleType,
            CarParkCandidate candidate,
            List<LotAvailability> lots) {
        if (lots == null) {
            return java.util.Optional.empty();
        }
        LotAvailability matching = lots.stream()
                .filter(lot -> lot.vehicleType() == vehicleType && lot.availableLots() > 0)
                .findFirst()
                .orElse(null);
        if (matching == null) {
            return java.util.Optional.empty();
        }
        var carPark = candidate.carPark();
        return java.util.Optional.of(new CarParkSearchResult(
                carPark.carparkNumber(), carPark.address(), vehicleType, candidate.distanceMeters(),
                matching.availableLots(), matching.totalLots()));
    }
}
