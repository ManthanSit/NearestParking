package sg.carpark.integration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import sg.carpark.availability.SourceAvailabilitySnapshot;
import sg.carpark.catalog.SourceCarPark;
import sg.carpark.config.DataGovSgProperties;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("live")
@SpringBootTest(
        classes = DataGovSgLiveIntegrationTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "management.endpoint.health.group.readiness.include=readinessState")
class DataGovSgLiveIntegrationTest {
    @Autowired
    private DataGovSgClient client;

    @Test
    @EnabledIfSystemProperty(named = "runLiveDataGovTests", matches = "true")
    void fetchesHdbCatalogueFromDataGovSg() {
        List<SourceCarPark> catalogue = client.fetchCatalogue();

        assertThat(catalogue).isNotEmpty();
        assertThat(catalogue).allSatisfy(carPark -> {
            assertThat(carPark.carparkNumber()).isNotBlank();
            assertThat(carPark.address()).isNotBlank();
        });
    }

    @Test
    @EnabledIfSystemProperty(named = "runLiveDataGovTests", matches = "true")
    void fetchesAvailabilitySnapshotFromDataGovSg() {
        SourceAvailabilitySnapshot snapshot = client.fetchAvailability();

        assertThat(snapshot.sourceTimestamp()).isNotNull();
        assertThat(snapshot.lots()).isNotEmpty();
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EnableConfigurationProperties(DataGovSgProperties.class)
    @Import(DataGovSgClient.class)
    static class TestApplication {
    }
}
