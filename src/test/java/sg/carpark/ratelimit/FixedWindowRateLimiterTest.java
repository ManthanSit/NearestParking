package sg.carpark.ratelimit;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FixedWindowRateLimiterTest {
    @Test
    void allowsThirtySearchesThenRejectsUntilNextMinute() {
        FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(new InMemoryCounterStore(), 30, 60);
        Instant duringWindow = Instant.parse("2026-10-01T12:34:15Z");

        for (int i = 0; i < 30; i++) {
            assertTrue(limiter.check("192.0.2.10", duringWindow).allowed());
        }
        assertFalse(limiter.check("192.0.2.10", duringWindow).allowed());
        assertTrue(limiter.check("192.0.2.10", Instant.parse("2026-10-01T12:35:00Z")).allowed());
    }

    private static final class InMemoryCounterStore implements RateLimitCounterStore {
        private final Map<String, Long> counts = new HashMap<>();

        @Override
        public long increment(String key, long ttlSeconds) {
            return counts.merge(key, 1L, Long::sum);
        }
    }
}
