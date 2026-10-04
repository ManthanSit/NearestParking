package sg.carpark.catalog;

import sg.carpark.domain.CarPark;

public record CarParkCandidate(CarPark carPark, double distanceMeters) {
}
