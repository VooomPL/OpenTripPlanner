package org.opentripplanner.routing.edgetype;

import org.opentripplanner.routing.vertextype.CityGovVehicleDropoffStationVertex;
import org.opentripplanner.routing.vertextype.StreetVertex;

public class CityGovVehicleDropoffStationLink extends FreeEdge {

    public CityGovVehicleDropoffStationLink(StreetVertex fromv, CityGovVehicleDropoffStationVertex tov) {
        super(fromv, tov);
    }

    public CityGovVehicleDropoffStationLink(CityGovVehicleDropoffStationVertex fromv, StreetVertex tov) {
        super(fromv, tov);
    }
}
