package org.opentripplanner.routing.edgetype;

import org.opentripplanner.routing.vertextype.MobilityHubVehicleDropoffStationVertex;
import org.opentripplanner.routing.vertextype.StreetVertex;

public class MobilityHubVehicleDropoffStationLink extends FreeEdge {

    public MobilityHubVehicleDropoffStationLink(StreetVertex fromv, MobilityHubVehicleDropoffStationVertex tov) {
        super(fromv, tov);
    }

    public MobilityHubVehicleDropoffStationLink(MobilityHubVehicleDropoffStationVertex fromv, StreetVertex tov) {
        super(fromv, tov);
    }
}
