package sg.carpark.catalog;

import org.junit.jupiter.api.Test;
import sg.carpark.domain.CarPark;
import sg.carpark.domain.Wgs84Coordinate;
import sg.carpark.geo.Svy21CoordinateConverter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CarParkCatalogServiceTest {
    @Test
    void skipsWebsiteFetchWhenRedisAlreadyHasCatalogue() {
        FakeCatalogRepository repository = new FakeCatalogRepository();
        repository.catalogue = List.of(new CarPark("A", "Address A", 103.8, 1.3));
        FakeCatalogSource source = new FakeCatalogSource(List.of());

        service(repository, source).ensureLoaded();

        assertEquals(0, source.fetchCount);
        assertEquals(1, repository.catalogue.size());
    }

    @Test
    void fetchesAndPublishesCatalogueWhenRedisIsEmpty() {
        FakeCatalogRepository repository = new FakeCatalogRepository();
        FakeCatalogSource source = new FakeCatalogSource(List.of(
                new SourceCarPark("A", "Address A", 26367.5806, 30069.2434)));

        service(repository, source).ensureLoaded();

        assertEquals(1, source.fetchCount);
        assertEquals("A", repository.catalogue.get(0).carparkNumber());
        assertTrue(repository.catalogue.get(0).longitude() > 103);
    }

    @Test
    void manualRefreshHonorsGlobalCooldownAndPreservesLastGoodCatalogueOnFailure() {
        FakeCatalogRepository repository = new FakeCatalogRepository();
        repository.catalogue = List.of(new CarPark("OLD", "Old", 103.8, 1.3));
        FakeCatalogSource source = new FakeCatalogSource(List.of());
        source.failure = new IllegalStateException("upstream unavailable");
        CarParkCatalogService service = service(repository, source);

        repository.allowRefresh = false;
        assertThrows(CatalogRefreshCooldownException.class, service::refreshManually);
        assertEquals(0, source.fetchCount);

        repository.allowRefresh = true;
        assertThrows(IllegalStateException.class, service::refreshManually);
        assertEquals("OLD", repository.catalogue.get(0).carparkNumber());
    }

    private static CarParkCatalogService service(FakeCatalogRepository repository, FakeCatalogSource source) {
        return new CarParkCatalogService(repository, source, new Svy21CoordinateConverter(), Duration.ofMinutes(10));
    }

    private static final class FakeCatalogRepository implements CarParkCatalogRepository {
        private List<CarPark> catalogue = List.of();
        private boolean allowRefresh = true;

        @Override
        public boolean hasCatalogue() {
            return !catalogue.isEmpty();
        }

        @Override
        public List<CarPark> getCatalogue() {
            return catalogue;
        }

        @Override
        public List<CarParkCandidate> findWithin(Wgs84Coordinate origin, double radiusKm) {
            return catalogue.stream().map(carPark -> new CarParkCandidate(carPark, 0)).toList();
        }

        @Override
        public void replaceCatalogue(List<CarPark> carParks) {
            catalogue = new ArrayList<>(carParks);
        }

        @Override
        public long acquireRefreshCooldown(Duration cooldown) {
            return allowRefresh ? 0 : cooldown.toSeconds();
        }
    }

    private static final class FakeCatalogSource implements CarParkCatalogSource {
        private final List<SourceCarPark> response;
        private int fetchCount;
        private RuntimeException failure;

        private FakeCatalogSource(List<SourceCarPark> response) {
            this.response = response;
        }

        @Override
        public List<SourceCarPark> fetchCatalogue() {
            fetchCount++;
            if (failure != null) {
                throw failure;
            }
            return response;
        }
    }
}
