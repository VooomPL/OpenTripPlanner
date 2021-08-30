package org.opentripplanner.routing.edgetype.rentedgetype;

import org.opentripplanner.routing.core.vehicle_sharing.VehicleDescription;

import java.io.Serializable;
import java.util.List;

import static java.util.Collections.emptyList;

public class MobilityHubParkingZoneInfo extends ParkingZoneInfo implements Serializable {

    public MobilityHubParkingZoneInfo(List<SingleParkingZone> parkingZones) {
        super(parkingZones, emptyList(), emptyList());
    }

    @Override
    protected boolean hasProviderAndVehicleTypeDisabledParkingZonesFeature(VehicleDescription vehicle) {
        return false;
    }
}
