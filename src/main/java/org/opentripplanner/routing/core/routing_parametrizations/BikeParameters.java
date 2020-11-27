package org.opentripplanner.routing.core.routing_parametrizations;

import org.opentripplanner.routing.core.vehicle_sharing.VehicleDescription;

import java.util.Objects;

/**
 * This class contains bicycle parameters. ALl rental parameters refer to old way of renting bicycles,
 * which is NOT COMPATIBLE with {@link VehicleDescription}.
 */
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

    public int getSwitchTime() {
        return switchTime;
    }

    public void setSwitchTime(int switchTime) {
        this.switchTime = switchTime;
    }

    public int getSwitchCost() {
        return switchCost;
    }

    public void setSwitchCost(int switchCost) {
        this.switchCost = switchCost;
    }

    public int getRentalPickupTime() {
        return rentalPickupTime;
    }

    public void setRentalPickupTime(int rentalPickupTime) {
        this.rentalPickupTime = rentalPickupTime;
    }

    public int getRentalPickupCost() {
        return rentalPickupCost;
    }

    public void setRentalPickupCost(int rentalPickupCost) {
        this.rentalPickupCost = rentalPickupCost;
    }

    public int getRentalDropoffTime() {
        return rentalDropoffTime;
    }

    public void setRentalDropoffTime(int rentalDropoffTime) {
        this.rentalDropoffTime = rentalDropoffTime;
    }

    public int getRentalDropoffCost() {
        return rentalDropoffCost;
    }

    public void setRentalDropoffCost(int rentalDropoffCost) {
        this.rentalDropoffCost = rentalDropoffCost;
    }

    public int getParkTime() {
        return parkTime;
    }

    public void setParkTime(int parkTime) {
        this.parkTime = parkTime;
    }

    public int getParkCost() {
        return parkCost;
    }

    public void setParkCost(int parkCost) {
        this.parkCost = parkCost;
    }

    public double getTriangleTimeFactor() {
        return triangleTimeFactor;
    }

    public void setTriangleTimeFactor(double triangleTimeFactor) {
        this.triangleTimeFactor = triangleTimeFactor;
    }

    public double getTriangleSlopeFactor() {
        return triangleSlopeFactor;
    }

    public void setTriangleSlopeFactor(double triangleSlopeFactor) {
        this.triangleSlopeFactor = triangleSlopeFactor;
    }

    public double getTriangleSafetyFactor() {
        return triangleSafetyFactor;
    }

    public void setTriangleSafetyFactor(double triangleSafetyFactor) {
        this.triangleSafetyFactor = triangleSafetyFactor;
    }

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

    public boolean isUseBikeRentalAvailabilityInformation() {
        return useBikeRentalAvailabilityInformation;
    }

    public void setUseBikeRentalAvailabilityInformation(boolean useBikeRentalAvailabilityInformation) {
        this.useBikeRentalAvailabilityInformation = useBikeRentalAvailabilityInformation;
    }

    public boolean isWalkingBike() {
        return walkingBike;
    }

    public void setWalkingBike(boolean walkingBike) {
        this.walkingBike = walkingBike;
    }

    public boolean isAllowBikeRental() {
        return allowBikeRental;
    }

    public void setAllowBikeRental(boolean allowBikeRental) {
        this.allowBikeRental = allowBikeRental;
    }

    public boolean isBikeParkAndRide() {
        return bikeParkAndRide;
    }

    public void setBikeParkAndRide(boolean bikeParkAndRide) {
        this.bikeParkAndRide = bikeParkAndRide;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BikeParameters that = (BikeParameters) o;
        return switchTime == that.switchTime &&
                switchCost == that.switchCost &&
                rentalPickupTime == that.rentalPickupTime &&
                rentalPickupCost == that.rentalPickupCost &&
                rentalDropoffTime == that.rentalDropoffTime &&
                rentalDropoffCost == that.rentalDropoffCost &&
                parkCost == that.parkCost &&
                parkTime == that.parkTime &&
                Double.compare(that.triangleTimeFactor, triangleTimeFactor) == 0 &&
                Double.compare(that.triangleSlopeFactor, triangleSlopeFactor) == 0 &&
                Double.compare(that.triangleSafetyFactor, triangleSafetyFactor) == 0 &&
                useBikeRentalAvailabilityInformation == that.useBikeRentalAvailabilityInformation &&
                walkingBike == that.walkingBike &&
                allowBikeRental == that.allowBikeRental &&
                bikeParkAndRide == that.bikeParkAndRide;
    }

    @Override
    public int hashCode() {
        return Objects.hash(switchTime, switchCost, rentalPickupTime, rentalPickupCost, rentalDropoffTime,
                rentalDropoffCost, parkCost, parkTime, triangleTimeFactor, triangleSlopeFactor, triangleSafetyFactor,
                useBikeRentalAvailabilityInformation, walkingBike, allowBikeRental, bikeParkAndRide);
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
