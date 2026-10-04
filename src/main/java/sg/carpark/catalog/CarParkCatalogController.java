package sg.carpark.catalog;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/carparks/catalog")
public class CarParkCatalogController {
    private final CarParkCatalogService service;

    public CarParkCatalogController(CarParkCatalogService service) { this.service = service; }

    @PostMapping("/refresh")
    public RefreshResponse refresh() {
        return new RefreshResponse(Instant.now(), service.refreshManually());
    }

    public record RefreshResponse(Instant updatedAt, int carparksLoaded) {}
}
