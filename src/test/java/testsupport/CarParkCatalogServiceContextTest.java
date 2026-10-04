package testsupport;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import sg.carpark.catalog.CarParkCandidate;
import sg.carpark.catalog.CarParkCatalogRepository;
import sg.carpark.catalog.CarParkCatalogService;
import sg.carpark.catalog.CarParkCatalogSource;
import sg.carpark.domain.CarPark;
import sg.carpark.domain.Wgs84Coordinate;
import sg.carpark.geo.Svy21CoordinateConverter;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CarParkCatalogServiceContextTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(ServiceConfiguration.class);

    @Test
    void selectsTheConfiguredServiceConstructor() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(CarParkCatalogService.class));
    }

    @Configuration(proxyBeanMethods = false)
    @Import(CarParkCatalogService.class)
    static class ServiceConfiguration {
        @Bean
        CarParkCatalogRepository catalogueRepository() {
            return new CarParkCatalogRepository() {
                public boolean hasCatalogue() { return false; }
                public List<CarPark> getCatalogue() { return List.of(); }
                public List<CarParkCandidate> findWithin(Wgs84Coordinate origin, double radiusKm) { return List.of(); }
                public void replaceCatalogue(List<CarPark> carParks) { }
                public long acquireRefreshCooldown(Duration cooldown) { return 0; }
            };
        }

        @Bean
        CarParkCatalogSource catalogueSource() { return List::of; }

        @Bean
        Svy21CoordinateConverter coordinateConverter() { return new Svy21CoordinateConverter(); }
    }
}
