package sg.carpark.ratelimit;

import java.time.Instant;

public class FixedWindowRateLimiter {
    private final RateLimitCounterStore counters;
    private final int limit;
    private final int windowSeconds;

    public FixedWindowRateLimiter(RateLimitCounterStore counters, int limit, int windowSeconds) {
        this.counters = counters;
        this.limit = limit;
        this.windowSeconds = windowSeconds;
    }

    public Decision check(String clientIp, Instant now) {
        long window = now.getEpochSecond() / windowSeconds;
        long resetAt = (window + 1) * windowSeconds;
        long count = counters.increment("carpark:ratelimit:search:" + window + ":" + clientIp, resetAt - now.getEpochSecond());
        return new Decision(count <= limit, count <= limit ? 0 : resetAt - now.getEpochSecond());
    }

    public record Decision(boolean allowed, long retryAfterSeconds) {}
}
