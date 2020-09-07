package org.opentripplanner.updater.vehicle_sharing.parking_zones;

import com.google.common.annotations.VisibleForTesting;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.opentripplanner.routing.edgetype.rentedgetype.ParkingZoneInfo;
import org.opentripplanner.routing.edgetype.rentedgetype.SingleParkingZone;
import org.opentripplanner.routing.graph.Vertex;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

public class ParkingZonesCalculator implements Serializable {

    private final List<GeometryParkingZone> geometryParkingZones;

    @VisibleForTesting
    final List<SingleParkingZone> parkingZonesEnabled;

    public ParkingZonesCalculator(List<GeometryParkingZone> geometryParkingZones) {
        this.geometryParkingZones = geometryParkingZones;
        this.parkingZonesEnabled = createParkingZonesEnabled();
    }

    private List<SingleParkingZone> createParkingZonesEnabled() {
        return geometryParkingZones.stream()
                .map(gpz -> new SingleParkingZone(gpz.getProviderId(), gpz.getVehicleType()))
                .distinct()
                .collect(toList());
    }

    public ParkingZoneInfo getParkingZonesForLocation(Vertex vertex) {
        Point point = createPoint(vertex);
        List<SingleParkingZone> parkingZones = geometryParkingZones.stream()
                .map(gpz -> findMatchingParkingZone(point, gpz))
                .filter(Objects::nonNull)
                .collect(toList());
        return new ParkingZoneInfo(parkingZones, parkingZonesEnabled);
    }

    private Point createPoint(Vertex vertex) {
        CoordinateXY coord = new CoordinateXY(vertex.getLon(), vertex.getLat());
        return new Point(new CoordinateArraySequence(new Coordinate[]{coord}), new GeometryFactory());
    }

    private SingleParkingZone findMatchingParkingZone(Point point, GeometryParkingZone geometryParkingZone) {
        if (isPointInParkingZone(point, geometryParkingZone)) {
            return getMatchingParkingZoneFromList(geometryParkingZone);
        } else {
            return null;
        }
    }

    private boolean isPointInParkingZone(Point point, GeometryParkingZone gpz) {
        return gpz.getGeometriesAllowed().stream().anyMatch(g -> g.contains(point))
                && gpz.getGeometriesDisallowed().stream().noneMatch(g -> g.contains(point));
    }

    private SingleParkingZone getMatchingParkingZoneFromList(GeometryParkingZone geometryParkingZone) {
        return parkingZonesEnabled.stream()
                .filter(pz -> pz.sameProviderIdAndVehicleType(geometryParkingZone))
                .findFirst()
                .orElse(null);
    }
}
