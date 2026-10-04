package sg.carpark.availability;

import org.junit.jupiter.api.Test;
import sg.carpark.domain.VehicleType;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AvailabilityIngestorTest {
    @Test
    void retainsLastSuccessfulSnapshotAndSourceTimestampOnFailure() {
        FakeAvailabilitySource source = new FakeAvailabilitySource();
        FakeAvailabilityRepository repository = new FakeAvailabilityRepository();
        AvailabilityIngestor ingestor = new AvailabilityIngestor(source, repository);

        ingestor.refreshOnce();
        AvailabilitySnapshot lastGood = repository.snapshot;

        source.failure = new IllegalStateException("upstream unavailable");
        ingestor.refreshOnce();

        assertEquals(lastGood, repository.snapshot);
        assertEquals(Instant.parse("2026-10-01T12:00:00Z"), repository.snapshot.sourceTimestamp());
    }

    private static final class FakeAvailabilitySource implements AvailabilitySource {
        private RuntimeException failure;

        @Override
        public SourceAvailabilitySnapshot fetchAvailability() {
            if (failure != null) {
                throw failure;
            }
            return new SourceAvailabilitySnapshot(Instant.parse("2026-10-01T12:00:00Z"), List.of(
                    new SourceLotAvailability("A", VehicleType.CAR, 7, 100)));
        }
    }

    private static final class FakeAvailabilityRepository implements AvailabilityRepository {
        private AvailabilitySnapshot snapshot;

        @Override
        public void publish(AvailabilitySnapshot newSnapshot) {
            snapshot = newSnapshot;
        }

        @Override
        public boolean hasSnapshot() {
            return snapshot != null;
        }

        @Override
        public AvailabilitySnapshot getSnapshot() {
            return snapshot;
        }
    }
}
