package org.opentripplanner.routing.edgetype.rentedgetype;

import org.opentripplanner.routing.core.vehicle_sharing.VehicleDescription;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleType;

import java.io.Serializable;
import java.util.List;

/**
 * This class enables disallowing dropping off vehicles outside of their parking zones (both made by provider and city
 * government). For each pair <{@link Provider}, {@link VehicleType}> there can be different parking zones. If a given
 * pair has parking zones feature enabled, then we will have it in `parkingZonesEnabled` field. If we can park a vehicle
 * of some type and some provider in a given location, then we will have this pair of <provider, vehicleType>
 * in `parkingZones` field;
 */
public class ParkingZoneInfo implements Serializable {

    /**
     * Are we inside a parking zone for given provider and vehicleType
     */
    private final List<SingleParkingZone> parkingZones;

    /**
     * Does this provider and vehicleType have parking zones feature enabled?
     */
    private final List<SingleParkingZone> parkingZonesEnabled;

    /**
     * Does city government forbid parking given vehicle types here?
     */
    private final List<VehicleType> vehicleTypesForbiddenFromParkingHere;

    public ParkingZoneInfo(List<SingleParkingZone> parkingZones, List<SingleParkingZone> parkingZonesEnabled,
                           List<VehicleType> vehicleTypesForbiddenFromParkingHere) {
        this.parkingZones = parkingZones;
        this.parkingZonesEnabled = parkingZonesEnabled;
        this.vehicleTypesForbiddenFromParkingHere = vehicleTypesForbiddenFromParkingHere;
    }

    /**
     * Checks if we can dropoff given vehicle at this location. We allow to dropoff a vehicle if both city government
     * and vehicle provider allows parking here
     */
    public boolean canDropoffVehicleHere(VehicleDescription vehicle) {
        return doesCityGovernmentAllowParkingHere(vehicle) && doesProviderAllowParkingHere(vehicle);
    }

    /**
     * Checks if city government allows parking given vehicle at this location. We allow to dropoff a vehicle if
     * 1. This city does not have any areas in which we cannot park a vehicle
     * or
     * 2. We are outside of those areas
     */
    private boolean doesCityGovernmentAllowParkingHere(VehicleDescription vehicle) {
        return !vehicleTypesForbiddenFromParkingHere.contains(vehicle.getVehicleType());
    }

    /**
     * Checks if vehicle provider allows parking given vehicle at this location. We allow to dropoff a vehicle if
     * 1. Their provider and vehicle type have disabled parking zones feature
     * or
     * 2. We are inside parking zone for that provider and vehicleType
     */
    private boolean doesProviderAllowParkingHere(VehicleDescription vehicle) {
        return hasProviderAndVehicleTypeDisabledParkingZonesFeature(vehicle)
                || areWeInsideParkingZoneForProviderAndVehicleType(vehicle);
    }

    private boolean hasProviderAndVehicleTypeDisabledParkingZonesFeature(VehicleDescription vehicle) {
        if (vehicle.requiresHubToDrop())
            return false;
        return parkingZonesEnabled.stream().noneMatch(pz -> pz.appliesToThisVehicle(vehicle));
    }

    private boolean areWeInsideParkingZoneForProviderAndVehicleType(VehicleDescription vehicle) {
        return parkingZones.stream().anyMatch(pz -> pz.appliesToThisVehicle(vehicle));
    }

}
