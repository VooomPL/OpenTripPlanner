package org.opentripplanner.routing.edgetype.rentedgetype;

import lombok.Value;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleDescription;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleType;
import org.opentripplanner.updater.vehicle_sharing.parking_zones.GeometryParkingZone;

import java.io.Serializable;

/**
 * Represents a provider parking zone - if a provider allows/disallows parking here their vehicles of a given type
 */
@Value
public class SingleParkingZone implements Serializable {

    int providerId;

    VehicleType vehicleType;

    public boolean sameProviderIdAndVehicleType(GeometryParkingZone geometryParkingZone) {
        return providerId == geometryParkingZone.getProviderId()
                && vehicleType.equals(geometryParkingZone.getVehicleType());
    }

    boolean appliesToThisVehicle(VehicleDescription vehicle) {
        return vehicle.getProvider().getProviderId() == providerId && vehicle.getVehicleType().equals(vehicleType);
    }
}
