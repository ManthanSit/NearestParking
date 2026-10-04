package sg.carpark.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import sg.carpark.availability.AvailabilityIngestor;
import sg.carpark.catalog.CarParkCatalogService;

@Configuration
@EnableScheduling
public class StartupTasks {
    private static final Logger logger = LoggerFactory.getLogger(StartupTasks.class);

    @Bean
    ApplicationRunner catalogueLoader(CarParkCatalogService service) {
        return (ApplicationArguments args) -> {
            try { service.ensureLoaded(); }
            catch (RuntimeException exception) { logger.warn("Catalogue is not available yet", exception); }
        };
    }

    @Bean
    AvailabilityPollingTask availabilityPollingTask(AvailabilityIngestor ingestor) {
        return new AvailabilityPollingTask(ingestor);
    }

    static class AvailabilityPollingTask {
        private final AvailabilityIngestor ingestor;
        AvailabilityPollingTask(AvailabilityIngestor ingestor) { this.ingestor = ingestor; }
        @Scheduled(fixedDelayString = "${carpark.availability.poll-interval-ms:60000}", initialDelay = 0)
        public void poll() { ingestor.refreshOnce(); }
    }
}
