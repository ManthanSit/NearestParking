package sg.carpark.ratelimit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RedisRateLimitCounterStore implements RateLimitCounterStore {
    private static final DefaultRedisScript<Long> INCREMENT = new DefaultRedisScript<>(
            "local n = redis.call('INCR', KEYS[1]); if n == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]); end; return n", Long.class);
    private final StringRedisTemplate redis;

    public RedisRateLimitCounterStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public long increment(String key, long ttlSeconds) {
        Long value = redis.execute(INCREMENT, List.of(key), Long.toString(ttlSeconds));
        return value == null ? Long.MAX_VALUE : value;
    }
}
