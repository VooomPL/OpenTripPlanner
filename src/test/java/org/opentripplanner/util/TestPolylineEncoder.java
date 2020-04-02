package org.opentripplanner.util;

import junit.framework.TestCase;
import org.locationtech.jts.geom.Coordinate;
import org.opentripplanner.util.model.EncodedPolylineBean;

import java.util.ArrayList;
import java.util.List;

public class TestPolylineEncoder extends TestCase {

    public void testCreateEncodingsIterableOfCoordinate() {
        // test taken from example usage
        List<Coordinate> points = new ArrayList<Coordinate>();
        points.add(new Coordinate(-73.85062, 40.903125, Double.NaN));
        points.add(new Coordinate(-73.85136, 40.902261, Double.NaN));
        points.add(new Coordinate(-73.85151, 40.902066, Double.NaN));
        EncodedPolylineBean eplb = PolylineEncoder.createEncodings(points);
        assertEquals("o{sxFl}vaMjDpCf@\\", eplb.getPoints());
        assertEquals(3, eplb.getLength());
        assertNull(eplb.getLevels());
    }
}
