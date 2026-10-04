package sg.carpark.search;

import org.junit.jupiter.api.Test;
import sg.carpark.domain.CarPark;
import sg.carpark.domain.LotAvailability;
import sg.carpark.domain.VehicleType;
import sg.carpark.catalog.CarParkCandidate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NearestCarParkSelectorTest {
    private final NearestCarParkSelector selector = new NearestCarParkSelector();

    @Test
    void preservesRedisDistanceAndAscendingOrderWhenFilteringAvailableLots() {
        List<CarParkCandidate> candidates = List.of(
                candidate("FULL", 25.5),
                candidate("NEAR", 101.25),
                candidate("FAR", 2200.75));
        Map<String, List<LotAvailability>> availability = Map.of(
                "FULL", List.of(lots(VehicleType.CAR, 0, 100)),
                "NEAR", List.of(lots(VehicleType.CAR, 4, 100), lots(VehicleType.MOTORCYCLE, 12, 50)),
                "FAR", List.of(lots(VehicleType.CAR, 7, 100)));

        List<CarParkSearchResult> results = selector.select(
                VehicleType.CAR, 2, candidates, availability);

        assertEquals(List.of("NEAR", "FAR"), results.stream().map(CarParkSearchResult::carparkNumber).toList());
        assertEquals(List.of(101.25, 2200.75), results.stream().map(CarParkSearchResult::distanceMeters).toList());
    }

    @Test
    void filtersBySelectedVehicleTypeAndReturnsEmptyWhenNothingMatches() {
        List<CarParkSearchResult> results = selector.select(
                VehicleType.HEAVY,
                3,
                List.of(candidate("A", 100)),
                Map.of("A", List.of(lots(VehicleType.CAR, 20, 100))));

        assertTrue(results.isEmpty());
    }

    private static CarPark carPark(String number, double longitude, double latitude) {
        return new CarPark(number, "Address " + number, longitude, latitude);
    }

    private static CarParkCandidate candidate(String number, double distanceMeters) {
        return new CarParkCandidate(carPark(number, 0, 0), distanceMeters);
    }

    private static LotAvailability lots(VehicleType type, int available, int total) {
        return new LotAvailability(type, available, total);
    }
}
