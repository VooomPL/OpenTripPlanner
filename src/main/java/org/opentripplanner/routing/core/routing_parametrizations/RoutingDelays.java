package org.opentripplanner.routing.core.routing_parametrizations;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleDescription;

/**
 * Describes how long specified actions take.
 */
@Getter
@Setter
@EqualsAndHashCode
public class RoutingDelays implements Cloneable {

    private int kickScooterRentingTime = 30;

    private int kickScooterDropoffTime = 30;

    private int motorbikeRentingTime = 60;

    private int motorbikeDropoffTime = 60;

    private int carRentingTime = 90;

    private int bikeDropoffTime = 30;

    private int bikeRentingTime = 120;

    private int carDropoffTime = 240;

    public int getRentingTime(VehicleDescription vehicleDescription) {
        switch (vehicleDescription.getVehicleType()) {
            case CAR:
                return carRentingTime;
            case MOTORBIKE:
                return motorbikeRentingTime;
            case KICKSCOOTER:
                return kickScooterRentingTime;
            case BIKE:
                return bikeRentingTime;
            default:
                return 0;
        }
    }

    public int getDropoffTime(VehicleDescription vehicleDescription) {
        switch (vehicleDescription.getVehicleType()) {
            case CAR:
                return carDropoffTime;
            case MOTORBIKE:
                return motorbikeDropoffTime;
            case KICKSCOOTER:
                return kickScooterDropoffTime;
            case BIKE:
                return bikeDropoffTime;
            default:
                throw new IllegalArgumentException("Dropoff time is not specified for this vehicle type");
        }
    }

    public RoutingDelays clone() {
        try {
            return (RoutingDelays) super.clone();
        } catch (CloneNotSupportedException e) {
            /* this will never happen since our super is the cloneable object */
            throw new RuntimeException(e);
        }
    }
}
