package org.opentripplanner.updater.vehicle_sharing.parking_zones;

import lombok.Value;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleType;

import java.io.Serializable;
import java.util.List;

@Value
public class GeometryParkingZone implements Serializable {

    int providerId;
    VehicleType vehicleType;
    List<Geometry> geometriesAllowed;
    List<Geometry> geometriesDisallowed;

    public boolean isPointInParkingZone(Point point) {
        return geometriesAllowed.stream().anyMatch(g -> g.contains(point))
                && geometriesDisallowed.stream().noneMatch(g -> g.contains(point));
    }

    public boolean sameProviderIdAndVehicleType(GeometryParkingZone other) {
        return providerId == other.providerId && vehicleType == other.vehicleType;
    }
}
