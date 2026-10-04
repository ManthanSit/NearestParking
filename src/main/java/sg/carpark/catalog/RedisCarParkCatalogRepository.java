package sg.carpark.catalog;

import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.data.redis.domain.geo.GeoShape;
import org.springframework.stereotype.Repository;
import sg.carpark.domain.CarPark;
import sg.carpark.domain.Wgs84Coordinate;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class RedisCarParkCatalogRepository implements CarParkCatalogRepository {
    private static final String ACTIVE_KEY = "carpark:catalog:active";
    private static final String COOLDOWN_KEY = "carpark:catalog:refresh-cooldown";
    private final StringRedisTemplate redis;
    private final JsonMapper json;

    public RedisCarParkCatalogRepository(StringRedisTemplate redis, JsonMapper json) {
        this.redis = redis;
        this.json = json;
    }

    @Override
    public boolean hasCatalogue() {
        return redis.hasKey(ACTIVE_KEY) && !getCatalogue().isEmpty();
    }

    @Override
    public List<CarPark> getCatalogue() {
        String version = redis.opsForValue().get(ACTIVE_KEY);
        if (version == null) return List.of();
        String data = redis.opsForValue().get(dataKey(version));
        if (data == null) return List.of();
        return json.readValue(data, new TypeReference<List<CarPark>>() {});
    }

    @Override
    public List<CarParkCandidate> findWithin(Wgs84Coordinate origin, double radiusKm) {
        String version = redis.opsForValue().get(ACTIVE_KEY);
        if (version == null) return List.of();
        String key = geoKey(version);
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = redis.opsForGeo().search(
                key, GeoReference.fromCoordinate(origin.longitude(), origin.latitude()),
                GeoShape.byRadius(new Distance(radiusKm, Metrics.KILOMETERS)),
                RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs().includeDistance().sortAscending());
        if (results == null) return List.of();
        Map<String, CarPark> catalogue = getCatalogue().stream()
                .collect(Collectors.toMap(CarPark::carparkNumber, Function.identity(), (first, ignored) -> first));
        return results.getContent().stream()
                .map(result -> {
                    CarPark carPark = catalogue.get(result.getContent().getName());
                    if (carPark == null) return null;
                    double distanceMeters = result.getDistance().in(Metrics.KILOMETERS).getValue() * 1000;
                    return new CarParkCandidate(carPark, distanceMeters);
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Override
    public void replaceCatalogue(List<CarPark> carParks) {
        String version = UUID.randomUUID().toString();
        String dataKey = dataKey(version);
        String geoKey = geoKey(version);
        redis.opsForValue().set(dataKey, json.writeValueAsString(carParks));
        Map<String, CarPark> unique = carParks.stream().collect(Collectors.toMap(
                CarPark::carparkNumber, Function.identity(), (first, ignored) -> first));
        unique.values().forEach(carPark -> redis.opsForGeo().add(geoKey,
                new org.springframework.data.geo.Point(carPark.longitude(), carPark.latitude()), carPark.carparkNumber()));
        redis.opsForValue().set(ACTIVE_KEY, version);
    }

    @Override
    public long acquireRefreshCooldown(Duration cooldown) {
        Boolean acquired = redis.opsForValue().setIfAbsent(COOLDOWN_KEY, "1", cooldown);
        if (Boolean.TRUE.equals(acquired)) return 0;
        Long ttl = redis.getExpire(COOLDOWN_KEY);
        return ttl == null || ttl < 0 ? cooldown.toSeconds() : ttl;
    }

    private static String dataKey(String version) { return "carpark:catalog:" + version + ":data"; }
    private static String geoKey(String version) { return "carpark:catalog:" + version + ":geo"; }
}
