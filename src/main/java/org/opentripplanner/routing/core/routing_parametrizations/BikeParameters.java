package org.opentripplanner.routing.core.routing_parametrizations;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleDescription;

/**
 * This class contains bicycle parameters. ALl rental parameters refer to old way of renting bicycles,
 * which is NOT COMPATIBLE with {@link VehicleDescription}.
 */
@Getter
@Setter
@EqualsAndHashCode
public class BikeParameters implements Cloneable {

    /**
     * Time to get on and off your own bike
     */
    private int switchTime;

    /**
     * Cost of getting on and off your own bike
     */
    private int switchCost;

    /**
     * Time to rent a bike
     */
    private int rentalPickupTime = 60;

    /**
     * Cost of renting a bike. The cost is a bit more than actual time to model the associated cost and trouble.
     */
    private int rentalPickupCost = 120;

    /**
     * Time to drop-off a rented bike
     */
    private int rentalDropoffTime = 30;

    /**
     * Cost of dropping-off a rented bike
     */
    private int rentalDropoffCost = 30;

    /**
     * Cost of parking a bike.
     */
    private int parkCost = 120;

    /**
     * Time to park a bike
     */
    private int parkTime = 60;

    /**
     * For the bike triangle, how important time is.
     * triangleTimeFactor+triangleSlopeFactor+triangleSafetyFactor == 1
     */
    private double triangleTimeFactor;

    /**
     * For the bike triangle, how important slope is
     */
    private double triangleSlopeFactor;

    /**
     * For the bike triangle, how important safety is
     */
    private double triangleSafetyFactor;

    /**
     * Whether or not bike rental availability information will be used to plan bike rental trips
     */
    private boolean useBikeRentalAvailabilityInformation = false;

    private boolean walkingBike;

    /*
      Additional flags affecting mode transitions.
      This is a temporary solution, as it only covers parking and rental at the beginning of the trip.
    */
    private boolean allowBikeRental = false;

    private boolean bikeParkAndRide = false;

    /**
     * Sets the bicycle triangle routing parameters -- the relative importance of safety, flatness, and speed.
     * These three fields of the RoutingRequest should have values between 0 and 1, and should add up to 1.
     * This setter function accepts any three numbers and will normalize them to add up to 1.
     */
    public void setTriangleNormalized(double safe, double slope, double time) {
        double total = safe + slope + time;
        triangleSafetyFactor = safe / total;
        triangleSlopeFactor = slope / total;
        triangleTimeFactor = time / total;
    }

    public BikeParameters clone() {
        try {
            return (BikeParameters) super.clone();
        } catch (CloneNotSupportedException e) {
            /* this will never happen since our super is the cloneable object */
            throw new RuntimeException(e);
        }
    }
}
