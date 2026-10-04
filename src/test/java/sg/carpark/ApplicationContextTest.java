package sg.carpark;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationContextTest {
        private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(NearestCarParkApplication.class)
            .withPropertyValues("spring.main.web-application-type=none");

    @Test
    void createsApplicationContext() {
        contextRunner.run(context -> assertThat(context).hasNotFailed());
    }
}
