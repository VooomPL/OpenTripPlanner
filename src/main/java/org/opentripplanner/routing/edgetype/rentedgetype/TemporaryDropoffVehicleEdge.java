package org.opentripplanner.routing.edgetype.rentedgetype;

import org.opentripplanner.routing.edgetype.TemporaryEdge;
import org.opentripplanner.routing.graph.Vertex;

public class TemporaryDropoffVehicleEdge extends RentVehicleAnywhereEdge implements TemporaryEdge {

    public TemporaryDropoffVehicleEdge(Vertex v) {
        super(v);
    }
}
