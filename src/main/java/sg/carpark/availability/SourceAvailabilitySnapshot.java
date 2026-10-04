package sg.carpark.availability;

import java.time.Instant;
import java.util.List;

public record SourceAvailabilitySnapshot(Instant sourceTimestamp, List<SourceLotAvailability> lots) {
}
