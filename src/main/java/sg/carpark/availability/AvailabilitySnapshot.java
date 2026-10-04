package sg.carpark.availability;

import sg.carpark.domain.LotAvailability;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record AvailabilitySnapshot(Instant sourceTimestamp, Map<String, List<LotAvailability>> byCarPark) {
}
