package org.opentripplanner.routing.vertextype;

import org.locationtech.jts.geom.Coordinate;
import org.opentripplanner.routing.location.StreetLocation;

public class TemporaryRentVehicleVertex extends StreetLocation implements TemporaryVertex {

    public TemporaryRentVehicleVertex(String id, Coordinate nearestPoint, String name) {
        super(id, nearestPoint, name);
    }

    @Override
    public boolean isEndVertex() {
        return false; // TODO
    }
}
