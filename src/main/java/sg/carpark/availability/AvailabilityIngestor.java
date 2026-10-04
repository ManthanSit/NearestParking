package sg.carpark.availability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sg.carpark.domain.LotAvailability;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AvailabilityIngestor {
    private static final Logger logger = LoggerFactory.getLogger(AvailabilityIngestor.class);
    private final AvailabilitySource source;
    private final AvailabilityRepository repository;

    public AvailabilityIngestor(AvailabilitySource source, AvailabilityRepository repository) {
        this.source = source;
        this.repository = repository;
    }

    public void refreshOnce() {
        try {
            SourceAvailabilitySnapshot sourceSnapshot = source.fetchAvailability();
            Map<String, List<LotAvailability>> byCarPark = sourceSnapshot.lots().stream()
                    .collect(Collectors.groupingBy(SourceLotAvailability::carparkNumber,
                            Collectors.mapping(lot -> new LotAvailability(
                                    lot.vehicleType(), lot.availableLots(), lot.totalLots()), Collectors.toList())));
            repository.publish(new AvailabilitySnapshot(sourceSnapshot.sourceTimestamp(), byCarPark));
        } catch (RuntimeException exception) {
            logger.warn("Availability refresh failed; retaining the last successful snapshot", exception);
        }
    }
}
