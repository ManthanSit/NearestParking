package sg.carpark.geo;

import org.junit.jupiter.api.Test;
import sg.carpark.domain.Wgs84Coordinate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Svy21CoordinateConverterTest {
    private final Svy21CoordinateConverter converter = new Svy21CoordinateConverter();

    @Test
    void convertsKnownSingaporeSvy21PositionToWgs84() {
        Wgs84Coordinate coordinate = converter.toWgs84(26367.5806, 30069.2434);

        assertEquals(103.817, coordinate.longitude(), 0.01);
        assertEquals(1.286, coordinate.latitude(), 0.01);
    }
}
