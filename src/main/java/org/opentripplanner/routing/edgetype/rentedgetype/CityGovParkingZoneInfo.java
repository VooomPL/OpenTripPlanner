package org.opentripplanner.routing.edgetype.rentedgetype;

import org.opentripplanner.routing.core.vehicle_sharing.VehicleDescription;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleType;

import java.util.List;

public class CityGovParkingZoneInfo {

    private final List<VehicleType> vehicleTypesForbiddenFromParkingHere;

    public CityGovParkingZoneInfo(List<VehicleType> vehicleTypesForbiddenFromParkingHere) {
        this.vehicleTypesForbiddenFromParkingHere = vehicleTypesForbiddenFromParkingHere;
    }

    // TODO AdamWiktor VMP-62 comments
    public boolean doesCityGovernmentAllowParkingHere(VehicleDescription vehicle) {
        return vehicleTypesForbiddenFromParkingHere.contains(vehicle.getVehicleType());
    }
}
