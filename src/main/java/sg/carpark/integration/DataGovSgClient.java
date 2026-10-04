package sg.carpark.integration;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import sg.carpark.availability.AvailabilitySource;
import sg.carpark.availability.SourceAvailabilitySnapshot;
import sg.carpark.availability.SourceLotAvailability;
import sg.carpark.catalog.CarParkCatalogSource;
import sg.carpark.catalog.SourceCarPark;
import sg.carpark.config.DataGovSgProperties;
import sg.carpark.domain.VehicleType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class DataGovSgClient implements CarParkCatalogSource, AvailabilitySource {
    private static final int CATALOGUE_PAGE_SIZE = 1_000;
    private static final ZoneId SINGAPORE = ZoneId.of("Asia/Singapore");

    private final RestClient restClient;
    private final DataGovSgProperties properties;

    public DataGovSgClient(DataGovSgProperties properties) {
        this.restClient = RestClient.create();
        this.properties = properties;
    }

    @Override
    public List<SourceCarPark> fetchCatalogue() {
        List<SourceCarPark> carParks = new ArrayList<>();
        for (int offset = 0; ; offset += CATALOGUE_PAGE_SIZE) {
            var uri = UriComponentsBuilder.fromUriString(properties.getCatalogueUrl())
                    .queryParam("resource_id", properties.getCatalogueResourceId())
                    .queryParam("limit", CATALOGUE_PAGE_SIZE)
                    .queryParam("offset", offset)
                    .build()
                    .encode()
                    .toUri();
            Map<?, ?> body = restClient.get().uri(uri).retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});
            List<?> records = asList(asMap(body.get("result")).get("records"));
            for (Object record : records) {
                Map<?, ?> row = asMap(record);
                carParks.add(new SourceCarPark(
                        string(row, "car_park_no", "Car Park No"),
                        string(row, "address", "Address"),
                        number(row, "x_coord", "X Coord"),
                        number(row, "y_coord", "Y Coord")));
            }
            if (records.size() < CATALOGUE_PAGE_SIZE) break;
        }
        if (carParks.isEmpty()) {
            throw new IllegalStateException("Data.gov.sg returned an empty HDB catalogue");
        }
        return carParks;
    }

    @Override
    public SourceAvailabilitySnapshot fetchAvailability() {
        Map<?, ?> body = restClient.get().uri(properties.getAvailabilityUrl()).retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
        List<?> items = asList(body.get("items"));
        if (items.isEmpty()) {
            throw new IllegalStateException("Data.gov.sg returned no availability item");
        }
        Map<?, ?> item = asMap(items.get(0));
        Instant sourceTimestamp = parseInstant(item.get("timestamp"));
        List<SourceLotAvailability> lots = new ArrayList<>();
        for (Object carparkItem : asList(item.get("carpark_data"))) {
            Map<?, ?> carpark = asMap(carparkItem);
            String carparkNumber = string(carpark, "carpark_number");
            for (Object infoItem : asList(carpark.get("carpark_info"))) {
                Map<?, ?> info = asMap(infoItem);
                VehicleType type = VehicleType.fromSourceCode(string(info, "lot_type"));
                if (type != null) {
                    lots.add(new SourceLotAvailability(carparkNumber, type,
                            integer(info, "lots_available"), integer(info, "total_lots")));
                }
            }
        }
        if (lots.isEmpty()) {
            throw new IllegalStateException("Data.gov.sg returned no supported availability lots");
        }
        return new SourceAvailabilitySnapshot(sourceTimestamp, lots);
    }

    private static Instant parseInstant(Object value) {
        String text = String.valueOf(value);
        try {
            return OffsetDateTime.parse(text).toInstant();
        } catch (RuntimeException ignored) {
            return LocalDateTime.parse(text).atZone(SINGAPORE).toInstant();
        }
    }

    private static Map<?, ?> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return map;
        }
        throw new IllegalStateException("Unexpected Data.gov.sg response shape");
    }

    private static List<?> asList(Object value) {
        if (value instanceof List<?> list) {
            return list;
        }
        return List.of();
    }

    private static String string(Map<?, ?> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null) {
                return String.valueOf(value);
            }
        }
        return "";
    }

    private static double number(Map<?, ?> map, String... keys) {
        return Double.parseDouble(string(map, keys));
    }

    private static int integer(Map<?, ?> map, String key) {
        return Integer.parseInt(string(map, key));
    }
}
