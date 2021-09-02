package org.opentripplanner.routing.edgetype.rentedgetype;

import org.opentripplanner.routing.edgetype.TemporaryEdge;
import org.opentripplanner.routing.graph.Vertex;

/**
 * This edge is used to allow dropping off rented vehicles at destination vertex. If we arrive at destination with some
 * rented vehicle, then cannot finnish spt query, because that state would not be final ( checked in `State#isFinal`).
 * However, we can drop off vehicle by traversing this edge, thus creating a final state.
 */
public class TemporaryHighCostDropoffVehicleEdge extends HighCostDropoffVehicleEdge implements TemporaryEdge {

    public TemporaryHighCostDropoffVehicleEdge(Vertex v) {
        super(v);
    }

    public TemporaryHighCostDropoffVehicleEdge(Vertex v, ParkingZoneInfo parkingZones) {
        super(v, parkingZones);
    }
}
