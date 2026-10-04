package sg.carpark.availability;

import sg.carpark.domain.VehicleType;

public record SourceLotAvailability(String carparkNumber, VehicleType vehicleType, int availableLots, int totalLots) {
}
