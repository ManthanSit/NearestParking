package sg.carpark.geo;

import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;
import org.springframework.stereotype.Component;
import sg.carpark.domain.Wgs84Coordinate;

@Component
public class Svy21CoordinateConverter {
    private final CoordinateTransform transform;

    public Svy21CoordinateConverter() {
        CRSFactory crsFactory = new CRSFactory();
        CoordinateReferenceSystem svy21 = crsFactory.createFromName("EPSG:3414");
        CoordinateReferenceSystem wgs84 = crsFactory.createFromName("EPSG:4326");
        transform = new CoordinateTransformFactory().createTransform(svy21, wgs84);
    }

    public Wgs84Coordinate toWgs84(double easting, double northing) {
        ProjCoordinate output = new ProjCoordinate();
        transform.transform(new ProjCoordinate(easting, northing), output);
        return new Wgs84Coordinate(output.x, output.y);
    }
}
