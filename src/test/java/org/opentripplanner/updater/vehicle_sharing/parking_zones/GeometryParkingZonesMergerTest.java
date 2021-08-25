package org.opentripplanner.updater.vehicle_sharing.parking_zones;

import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleType;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class GeometryParkingZonesMergerTest {

    private final GeometryParkingZonesMerger merger = new GeometryParkingZonesMerger();

    @Test
    public void shouldMergeParkingZones() {
        // given
        Geometry geometry1 = mock(Geometry.class);
        Geometry geometry2 = mock(Geometry.class);
        Geometry geometry3 = mock(Geometry.class);
        Geometry geometry4 = mock(Geometry.class);
        List<GeometryParkingZone> a = singletonList(
                new GeometryParkingZone(1, VehicleType.CAR, singletonList(geometry1), singletonList(geometry2))
        );
        List<GeometryParkingZone> b = singletonList(
                new GeometryParkingZone(1, VehicleType.CAR, singletonList(geometry3), singletonList(geometry4))
        );

        // when
        List<GeometryParkingZone> result = merger.merge(a, b);

        // then
        assertEquals(1, result.size());

        GeometryParkingZone merged = result.get(0);
        assertEquals(1, merged.getProviderId());
        assertEquals(VehicleType.CAR, merged.getVehicleType());

        assertEquals(2, merged.getGeometriesAllowed().size());
        assertTrue(merged.getGeometriesAllowed().contains(geometry1));
        assertTrue(merged.getGeometriesAllowed().contains(geometry3));

        assertEquals(2, merged.getGeometriesDisallowed().size());
        assertTrue(merged.getGeometriesDisallowed().contains(geometry2));
        assertTrue(merged.getGeometriesDisallowed().contains(geometry4));
    }

    @Test
    public void shouldIncludeParkingZonesFromBothLists() {
        // given
        Geometry geometry1 = mock(Geometry.class);
        Geometry geometry2 = mock(Geometry.class);
        Geometry geometry3 = mock(Geometry.class);
        Geometry geometry4 = mock(Geometry.class);
        GeometryParkingZone geometryParkingZone1 = new GeometryParkingZone(2, VehicleType.CAR, singletonList(geometry1), singletonList(geometry2));
        GeometryParkingZone geometryParkingZone2 = new GeometryParkingZone(3, VehicleType.MOTORBIKE, singletonList(geometry3), singletonList(geometry4));
        List<GeometryParkingZone> a = singletonList(geometryParkingZone1);
        List<GeometryParkingZone> b = singletonList(geometryParkingZone2);

        // when
        List<GeometryParkingZone> result = merger.merge(a, b);

        // then
        assertEquals(2, result.size());
        assertTrue(result.contains(geometryParkingZone1));
        assertTrue(result.contains(geometryParkingZone2));
    }
}
