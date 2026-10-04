package sg.carpark.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import sg.carpark.ratelimit.FixedWindowRateLimiter;
import sg.carpark.ratelimit.RateLimitCounterStore;

@Configuration
public class RateLimitConfiguration {
    @Bean
    FixedWindowRateLimiter fixedWindowRateLimiter(RateLimitCounterStore store, Environment environment) {
        return new FixedWindowRateLimiter(store,
                environment.getProperty("carpark.rate-limit.search-limit", Integer.class, 30),
                environment.getProperty("carpark.rate-limit.window-seconds", Integer.class, 60));
    }
}
