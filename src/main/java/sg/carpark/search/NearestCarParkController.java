package sg.carpark.search;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sg.carpark.domain.VehicleType;

@RestController
@RequestMapping("/api/v1/carparks")
@Validated
public class NearestCarParkController {
    private final NearestCarParkService service;

    public NearestCarParkController(NearestCarParkService service) { this.service = service; }

    @GetMapping("/nearest")
    public NearestCarParkService.SearchResponse nearest(
            @RequestParam @DecimalMin("-90") @DecimalMax("90") double latitude,
            @RequestParam @DecimalMin("-180") @DecimalMax("180") double longitude,
            @RequestParam String vehicleType,
            @RequestParam(defaultValue = "3") @DecimalMin(value = "0", inclusive = false) @DecimalMax("5") double radiusKm,
            @RequestParam(defaultValue = "3") @Min(1) @Max(10) int limit) {
        return service.search(latitude, longitude, VehicleType.fromApiValue(vehicleType), radiusKm, limit);
    }
}
