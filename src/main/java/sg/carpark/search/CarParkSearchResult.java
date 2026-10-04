package sg.carpark.search;

import sg.carpark.domain.VehicleType;

public record CarParkSearchResult(
        String carparkNumber,
        String address,
        VehicleType vehicleType,
        double distanceMeters,
        int availableLots,
        int totalLots) {
}
