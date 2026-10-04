package sg.carpark.ratelimit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(
        classes = RedisRateLimitIntegrationTest.TestApplication.class,
        properties = "management.endpoint.health.group.readiness.include=readinessState,redis")
class RedisRateLimitIntegrationTest {
    private static final Instant NOW = Instant.parse("2026-10-04T12:34:15Z");

    @Container
    static final GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:8-alpine"))
            .withExposedPorts(6379);

    @Autowired
    private RedisRateLimitCounterStore counters;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry properties) {
        properties.add("spring.data.redis.host", redis::getHost);
        properties.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Test
    void allowsThirtyRequestsThenRejectsTheNextUsingRedisCounters() {
        FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(counters, 30, 60);
        String clientIp = "192.0.2." + UUID.randomUUID();

        for (int request = 0; request < 30; request++) {
            assertThat(limiter.check(clientIp, NOW).allowed()).isTrue();
        }
        assertThat(limiter.check(clientIp, NOW).allowed()).isFalse();
        assertThat(limiter.check(clientIp, Instant.parse("2026-10-04T12:35:00Z")).allowed()).isTrue();
    }

    @Test
    void setsCounterExpiryOnceAndSharesCountsAcrossLimiterInstances() {
        String key = "test:rate-limit:" + UUID.randomUUID();
        assertThat(counters.increment(key, 120)).isEqualTo(1);
        long firstExpiry = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        assertThat(firstExpiry).isBetween(1L, 120L);

        assertThat(counters.increment(key, 600)).isEqualTo(2);
        assertThat(redisTemplate.getExpire(key, TimeUnit.SECONDS)).isLessThanOrEqualTo(firstExpiry);

        String clientIp = "192.0.2." + UUID.randomUUID();
        FixedWindowRateLimiter first = new FixedWindowRateLimiter(counters, 30, 60);
        FixedWindowRateLimiter second = new FixedWindowRateLimiter(
                new RedisRateLimitCounterStore(redisTemplate), 30, 60);
        for (int request = 0; request < 15; request++) {
            assertThat(first.check(clientIp, NOW).allowed()).isTrue();
            assertThat(second.check(clientIp, NOW).allowed()).isTrue();
        }
        assertThat(first.check(clientIp, NOW).allowed()).isFalse();
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(RedisRateLimitCounterStore.class)
    static class TestApplication {
    }
}
