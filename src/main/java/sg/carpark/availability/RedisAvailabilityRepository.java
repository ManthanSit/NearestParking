package sg.carpark.availability;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.json.JsonMapper;

@Repository
public class RedisAvailabilityRepository implements AvailabilityRepository {
    private static final String SNAPSHOT_KEY = "carpark:availability:latest";
    private final StringRedisTemplate redis;
    private final JsonMapper json;

    public RedisAvailabilityRepository(StringRedisTemplate redis, JsonMapper json) {
        this.redis = redis;
        this.json = json;
    }

    @Override
    public void publish(AvailabilitySnapshot snapshot) {
        redis.opsForValue().set(SNAPSHOT_KEY, json.writeValueAsString(snapshot));
    }

    @Override
    public boolean hasSnapshot() {
        return redis.hasKey(SNAPSHOT_KEY);
    }

    @Override
    public AvailabilitySnapshot getSnapshot() {
        String data = redis.opsForValue().get(SNAPSHOT_KEY);
        return data == null ? null : json.readValue(data, AvailabilitySnapshot.class);
    }
}
