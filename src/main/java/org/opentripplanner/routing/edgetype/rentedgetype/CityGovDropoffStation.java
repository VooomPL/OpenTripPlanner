package org.opentripplanner.routing.edgetype.rentedgetype;

import lombok.Value;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleType;

@Value
public class CityGovDropoffStation {

    double longitude, latitude;
    VehicleType vehicleType;
}
