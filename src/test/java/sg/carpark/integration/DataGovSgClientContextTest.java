package sg.carpark.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import sg.carpark.config.DataGovSgProperties;

import static org.assertj.core.api.Assertions.assertThat;

class DataGovSgClientContextTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(ClientConfiguration.class);

    @Test
    void createsTheDataGovClientFromConfiguredProperties() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(DataGovSgClient.class));
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(DataGovSgProperties.class)
    @Import(DataGovSgClient.class)
    static class ClientConfiguration {
    }
}
