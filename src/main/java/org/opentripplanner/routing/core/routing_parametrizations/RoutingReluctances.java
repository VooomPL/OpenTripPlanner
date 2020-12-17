package org.opentripplanner.routing.core.routing_parametrizations;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleType;

/**
 * Describes how bad time spend on specified actions is.
 */
@Getter
@Setter
@EqualsAndHashCode
public class RoutingReluctances {
    /**
     * How much worse is waiting for a transit vehicle than being on a transit vehicle, as a multiplier. The default value treats wait and on-vehicle
     * time as the same.
     * <p>
     * It may be tempting to set this higher than walkReluctance (as studies often find this kind of preferences among
     * riders) but the planner will take this literally and walk down a transit line to avoid waiting at a stop.
     * This used to be set less than 1 (0.95) which would make waiting offboard preferable to waiting onboard in an
     * interlined trip. That is also undesirable.
     * <p>
     * If we only tried the shortest possible transfer at each stop to neighboring stop patterns, this problem could disappear.
     */
    private double waitReluctance = 1.0;

    /**
     * How much less bad is waiting at the beginning of the trip (replaces waitReluctance on the first boarding)
     */
    private double waitAtBeginningFactor = 0.4;
    /**
     * A multiplier for how bad walking is, compared to being in transit for equal lengths of time.
     * Defaults to 2. Empirically, values between 10 and 20 seem to correspond well to the concept
     * of not wanting to walk too much without asking for totally ridiculous itineraries, but this
     * observation should in no way be taken as scientific or definitive. Your mileage may vary.
     */
    private double walkReluctance = 2.5;

    private double carReluctance = 1.0;

    private double motorbikeReluctance = 1.0;

    private double kickScooterReluctance = 1.5;

    private double bicycleReluctance = 1.5;
    /**
     * How much we hate picking up a vehicle/dropping it off
     */
    private double rentingReluctance = 3.0;


    public double getModeVehicleReluctance(VehicleType vehicleType, TraverseMode traverseMode) {
        if (traverseMode == TraverseMode.WALK) {
            return walkReluctance;
        } else if (vehicleType == VehicleType.CAR || traverseMode == TraverseMode.CAR) {
            return carReluctance;
        } else if (vehicleType == VehicleType.BIKE || traverseMode == TraverseMode.BICYCLE) {
            return bicycleReluctance;
        } else if (vehicleType == VehicleType.MOTORBIKE) {
            return motorbikeReluctance;
        } else if (vehicleType == VehicleType.KICKSCOOTER) {
            return kickScooterReluctance;
        } else {
            return 1.;
        }
    }

    public RoutingReluctances clone() {
        try {
            return (RoutingReluctances) super.clone();
        } catch (CloneNotSupportedException e) {
            /* this will never happen since our super is the cloneable object */
            throw new RuntimeException(e);
        }
    }
}
