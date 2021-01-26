package org.opentripplanner.routing.edgetype.rentedgetype;

import org.opentripplanner.routing.core.vehicle_sharing.VehicleType;

public class CityGovDropoffStation {

    private final double longitude;
    private final double latitude;
    private final VehicleType vehicleType;

    public CityGovDropoffStation(double longitude, double latitude, VehicleType vehicleType) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.vehicleType = vehicleType;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }
}
