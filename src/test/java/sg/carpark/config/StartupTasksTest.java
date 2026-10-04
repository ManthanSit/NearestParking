package sg.carpark.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.StandardEnvironment;
import sg.carpark.availability.AvailabilityRepository;
import sg.carpark.availability.AvailabilityIngestor;
import sg.carpark.availability.AvailabilitySnapshot;
import sg.carpark.availability.SourceAvailabilitySnapshot;
import sg.carpark.catalog.CarParkCatalogService;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StartupTasksTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(StartupTasks.class, Dependencies.class);

    @Test
    void createsBothStartupTasks() {
        contextRunner.run(context -> {
            assertThat(context).hasBean("catalogueLoader");
            assertThat(context).hasBean("availabilityPollingTask");
        });
    }

    @Configuration(proxyBeanMethods = false)
    static class Dependencies {
        @Bean
        CarParkCatalogService carParkCatalogService() {
            return new CarParkCatalogService(null, null, null, new StandardEnvironment());
        }

        @Bean
        AvailabilityIngestor availabilityIngestor() {
            return new AvailabilityIngestor(
                    () -> new SourceAvailabilitySnapshot(Instant.now(), List.of()),
                    new AvailabilityRepository() {
                        @Override
                        public void publish(AvailabilitySnapshot snapshot) {}

                        @Override
                        public boolean hasSnapshot() {
                            return false;
                        }

                        @Override
                        public AvailabilitySnapshot getSnapshot() {
                            return null;
                        }
                    });
        }
    }
}
