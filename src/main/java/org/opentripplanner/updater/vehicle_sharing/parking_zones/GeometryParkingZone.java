package org.opentripplanner.updater.vehicle_sharing.parking_zones;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleType;

import java.io.Serializable;
import java.util.List;

public class GeometryParkingZone implements Serializable {

    private final int providerId;

    private final VehicleType vehicleType;

    private final List<Geometry> geometriesAllowed;

    private final List<Geometry> geometriesDisallowed;

    public GeometryParkingZone(int providerId, VehicleType vehicleType, List<Geometry> geometriesAllowed,
                               List<Geometry> geometriesDisallowed
    ) {
        this.providerId = providerId;
        this.vehicleType = vehicleType;
        this.geometriesAllowed = geometriesAllowed;
        this.geometriesDisallowed = geometriesDisallowed;
    }

    public boolean isPointInParkingZone(Point point) {
        return geometriesAllowed.stream().anyMatch(g -> g.contains(point))
                && geometriesDisallowed.stream().noneMatch(g -> g.contains(point));
    }

    public int getProviderId() {
        return providerId;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public List<Geometry> getGeometriesAllowed() {
        return geometriesAllowed;
    }

    public List<Geometry> getGeometriesDisallowed() {
        return geometriesDisallowed;
    }
}
