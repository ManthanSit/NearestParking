package sg.carpark.catalog;

import java.util.List;

public interface CarParkCatalogSource {
    List<SourceCarPark> fetchCatalogue();
}
