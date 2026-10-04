package sg.carpark.ratelimit;

public interface RateLimitCounterStore {
    long increment(String key, long ttlSeconds);
}
