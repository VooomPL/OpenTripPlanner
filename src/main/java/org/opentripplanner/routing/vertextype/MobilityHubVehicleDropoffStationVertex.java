package org.opentripplanner.routing.vertextype;

import org.opentripplanner.routing.edgetype.rentedgetype.DropoffVehicleEdge;
import org.opentripplanner.routing.edgetype.rentedgetype.MobilityHubDropoffStation;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;

/**
 * These vertices represent special parking stations, called Mobility Hubs. We want users to return rented vehicles
 * in those stations rather than in short distance from them. On this vertex there should be only
 * a {@link DropoffVehicleEdge}, which allows dropping off vehicles that are generally not allowed to park in this area.
 */
public class MobilityHubVehicleDropoffStationVertex extends Vertex {

    public MobilityHubVehicleDropoffStationVertex(Graph g, MobilityHubDropoffStation station) {
        super(g, "Mobility hub vehicle dropoff station", station.getLongitude(),
                station.getLatitude());
    }
}
