package org.opentripplanner.updater.vehicle_sharing.parking_zones;

import lombok.Value;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleType;

import java.io.Serializable;
import java.util.List;

@Value
public class GeometriesDisallowedForVehicleType implements Serializable {

    VehicleType vehicleType;
    List<Geometry> geometriesDisallowed;

    public boolean isPointInDisallowedParkingZone(Point point) {
        return geometriesDisallowed.stream().anyMatch(g -> g.contains(point));
    }
}
