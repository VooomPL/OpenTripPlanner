package org.opentripplanner.updater.vehicle_sharing.parking_zones;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleType;

import java.io.Serializable;
import java.util.List;

public class GeometriesDisallowedForVehicleType implements Serializable {

    private final VehicleType vehicleType;

    private final List<Geometry> geometriesDisallowed;

    public GeometriesDisallowedForVehicleType(VehicleType vehicleType, List<Geometry> geometriesDisallowed) {
        this.vehicleType = vehicleType;
        this.geometriesDisallowed = geometriesDisallowed;
    }

    public boolean isPointInParkingZone(Point point) {
        return geometriesDisallowed.stream().noneMatch(g -> g.contains(point));
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public List<Geometry> getGeometriesDisallowed() {
        return geometriesDisallowed;
    }
}
