package org.opentripplanner.routing.vertextype;

import org.opentripplanner.routing.edgetype.rentedgetype.CityGovDropoffStation;
import org.opentripplanner.routing.edgetype.rentedgetype.DropoffVehicleEdge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;

/**
 * These vertices represent special parking stations made by city government inside forbidden parking zone also made by
 * city government. On this vertex there should be only a {@link DropoffVehicleEdge}, which allows dropping off vehicles
 * which are generally not allowed to park in this area.
 */
public class CityGovVehicleDropoffStationVertex extends Vertex {

    public CityGovVehicleDropoffStationVertex(Graph g, CityGovDropoffStation cityGovDropoffStation) {
        super(g, "City government vehicle dropoff station", cityGovDropoffStation.getLongitude(),
                cityGovDropoffStation.getLatitude());
    }
}
