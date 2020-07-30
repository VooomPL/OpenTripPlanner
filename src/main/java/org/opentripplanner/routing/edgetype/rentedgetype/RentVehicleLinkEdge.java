package org.opentripplanner.routing.edgetype.rentedgetype;

import org.opentripplanner.routing.edgetype.FreeEdge;
import org.opentripplanner.routing.edgetype.TemporaryEdge;
import org.opentripplanner.routing.graph.Vertex;

public class RentVehicleLinkEdge extends FreeEdge implements TemporaryEdge {

    public RentVehicleLinkEdge(Vertex from, Vertex to) {
        super(from, to);
    }
}
