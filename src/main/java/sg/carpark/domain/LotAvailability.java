package sg.carpark.domain;

public record LotAvailability(VehicleType vehicleType, int availableLots, int totalLots) {
}
