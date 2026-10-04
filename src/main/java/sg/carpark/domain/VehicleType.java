package sg.carpark.domain;

import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum VehicleType {
    CAR("car", "C"),
    MOTORCYCLE("motorcycle", "Y"),
    HEAVY("heavy", "H");

    private final String apiValue;
    private final String sourceCode;

    VehicleType(String apiValue, String sourceCode) {
        this.apiValue = apiValue;
        this.sourceCode = sourceCode;
    }

    @JsonValue
    public String apiValue() {
        return apiValue;
    }

    public String sourceCode() {
        return sourceCode;
    }

    @JsonCreator
    public static VehicleType fromApiValue(String value) {
        return Arrays.stream(values())
                .filter(type -> type.apiValue.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("vehicleType must be car, motorcycle, or heavy"));
    }

    public static VehicleType fromSourceCode(String value) {
        return Arrays.stream(values())
                .filter(type -> type.sourceCode.equalsIgnoreCase(value))
                .findFirst()
                .orElse(null);
    }
}
