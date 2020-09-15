package org.opentripplanner.routing.core.vehicle_sharing;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.math.BigDecimal;

public class VehiclePricingPackage {

    private final BigDecimal packagePrice;

    private final int packageTimeLimitInSeconds;

    private final int freeSeconds;

    private final BigDecimal minRentingPrice;

    private final BigDecimal startPrice;

    private final BigDecimal drivingPricePerTimeTickInPackage;

    private final BigDecimal parkingPricePerTimeTickInPackage;

    private final BigDecimal drivingPricePerTimeTickInPackageExceeded;

    private final BigDecimal parkingPricePerTimeTickInPackageExceeded;

    private final BigDecimal kilometerPrice;

    private final int secondsPerTimeTickInPackage;

    private final int secondsPerTimeTickInPackageExceeded;

    private final BigDecimal maxRentingPrice;

    private final boolean kilometerPriceEnabledAboveMaxRentingPrice;

    public VehiclePricingPackage(){
        /* By default creating a "no predefined package" configuration
         * (package time limit is set to 0, so we only use the package exceeded properties to compute the price)
         */
        this(BigDecimal.valueOf(20), 50, 10, BigDecimal.valueOf(5.99), BigDecimal.valueOf(2.59), BigDecimal.valueOf(0.65), BigDecimal.ZERO, BigDecimal.valueOf(1.29), BigDecimal.ZERO, BigDecimal.valueOf(3.5), 60, 60, BigDecimal.ZERO, false);
    }

    public VehiclePricingPackage(BigDecimal packagePrice, int packageTimeLimitInSeconds, int freeSeconds, BigDecimal minRentingPrice, BigDecimal startPrice, BigDecimal drivingPricePerTimeTickInPackage, BigDecimal parkingPricePerTimeTickInPackage, BigDecimal drivingPricePerTimeTickInPackageExceeded, BigDecimal parkingPricePerTimeTickPackageExceeded, BigDecimal kilometerPrice, int secondsPerTimeTickInPackage, int secondsPerTimeTickInPackageExceeded, BigDecimal maxRentingPrice, boolean kilometerPriceEnabledAboveMaxRentingPrice) {
        this.packagePrice = packagePrice;
        this.packageTimeLimitInSeconds = packageTimeLimitInSeconds;
        this.freeSeconds = freeSeconds;
        this.minRentingPrice = minRentingPrice;
        this.startPrice = startPrice;
        this.drivingPricePerTimeTickInPackage = drivingPricePerTimeTickInPackage;
        this.parkingPricePerTimeTickInPackage = parkingPricePerTimeTickInPackage;
        this.drivingPricePerTimeTickInPackageExceeded = drivingPricePerTimeTickInPackageExceeded;
        this.parkingPricePerTimeTickInPackageExceeded = parkingPricePerTimeTickPackageExceeded;
        this.kilometerPrice = kilometerPrice;
        this.secondsPerTimeTickInPackage = secondsPerTimeTickInPackage>0?secondsPerTimeTickInPackage:1;
        this.secondsPerTimeTickInPackageExceeded = secondsPerTimeTickInPackageExceeded>0?secondsPerTimeTickInPackageExceeded:1;
        this.maxRentingPrice = maxRentingPrice;
        this.kilometerPriceEnabledAboveMaxRentingPrice = kilometerPriceEnabledAboveMaxRentingPrice;
    }

    public BigDecimal computeStartPrice(){
        return packagePrice.add(startPrice);
    }

    public BigDecimal computeTimeAssociatedPriceChange(BigDecimal currentTotalVehiclePrice, int remainingFreeSeconds, int totalDrivingTimeInSeconds, int timeChangeInSeconds){
        if (maxRentingPrice.compareTo(BigDecimal.ZERO) == 0 || //max renting time not used
                (maxRentingPrice.compareTo(BigDecimal.ZERO) > 0 && //max renting time used, but not exceeded
                        currentTotalVehiclePrice.compareTo(maxRentingPrice) < 0)) {
            /* Assuming that before entering this method:
               totalDrivingTimeInSeconds = previous totalDrivingTimeInSeconds + timeChangeInSeconds
               (so it does not take into account free seconds)
            */
            BigDecimal priceChange = BigDecimal.ZERO;
            if (remainingFreeSeconds > 0) {
                timeChangeInSeconds -= remainingFreeSeconds;
            }
            if (timeChangeInSeconds > 0) {
                if (totalDrivingTimeInSeconds <= packageTimeLimitInSeconds + freeSeconds) {
                    //all the seconds are included in the package
                    priceChange = (BigDecimal.valueOf(timeChangeInSeconds).divide(BigDecimal.valueOf(secondsPerTimeTickInPackage), 3, BigDecimal.ROUND_HALF_UP)).multiply(drivingPricePerTimeTickInPackage);
                } else {
                    //at least some seconds are above package limit
                    if((totalDrivingTimeInSeconds - timeChangeInSeconds) < (packageTimeLimitInSeconds + freeSeconds)){
                        //some seconds from the time change should be a part of the package
                        int secondsInPackage = (packageTimeLimitInSeconds + freeSeconds) - (totalDrivingTimeInSeconds - timeChangeInSeconds);
                        priceChange = (BigDecimal.valueOf(secondsInPackage).divide(BigDecimal.valueOf(secondsPerTimeTickInPackage), 3, BigDecimal.ROUND_HALF_UP)).multiply(drivingPricePerTimeTickInPackage);
                        timeChangeInSeconds -= secondsInPackage;
                    }
                    //the rest of the seconds should be treated as above package
                    priceChange = priceChange.add((BigDecimal.valueOf(timeChangeInSeconds).divide(BigDecimal.valueOf(secondsPerTimeTickInPackageExceeded), 3, BigDecimal.ROUND_HALF_UP)).multiply(drivingPricePerTimeTickInPackageExceeded));
                }
            }
            if (maxRentingPrice.compareTo(BigDecimal.ZERO) == 0){ //max renting time not used
                return priceChange;
            }
            else{ //max renting time used, check whether it is going to be exceeded after adding price change
                return currentTotalVehiclePrice.add(priceChange).compareTo(maxRentingPrice) < 1 ? priceChange : maxRentingPrice.subtract(currentTotalVehiclePrice);
            }
        } else { //max renting time used and exceeded
            return BigDecimal.ZERO;
        }
    }

    public int computeRemainingFreeSeconds(int remainingFreeSeconds, int timeChangeInSeconds){
        if(remainingFreeSeconds<0){
            return freeSeconds;
        }
        else{
            int newRemainingFreeSeconds = remainingFreeSeconds-timeChangeInSeconds;
            return newRemainingFreeSeconds>0?newRemainingFreeSeconds:0;
        }
    }

    public BigDecimal computeDistanceAssociatedPriceChange(BigDecimal currentTotalVehiclePrice, double previousDistanceInMeters, double distanceModificationInMeters){
        if (maxRentingPrice.compareTo(BigDecimal.ZERO) == 0 || //max renting time not used
                (maxRentingPrice.compareTo(BigDecimal.ZERO) > 0 && //max renting time used ...
                        (currentTotalVehiclePrice.compareTo(maxRentingPrice) < 0) || //... but not exceeded
                        kilometerPriceEnabledAboveMaxRentingPrice)) // ...or counting full kilometer price anyway
        {
            int previousDistanceInKilometers = (int) (previousDistanceInMeters / 1000);
            int newDistanceInKilometers = (int) ((previousDistanceInMeters + distanceModificationInMeters) / 1000);
            BigDecimal priceChange = (new BigDecimal(newDistanceInKilometers - previousDistanceInKilometers)).multiply(kilometerPrice);
            if (maxRentingPrice.compareTo(BigDecimal.ZERO) == 0 || kilometerPriceEnabledAboveMaxRentingPrice){
                // max renting time not used or counting full kilometer price anyway
                return priceChange;
            } else{ //max renting time used, check whether it is going to be exceeded after adding price change
                return currentTotalVehiclePrice.add(priceChange).compareTo(maxRentingPrice) < 1 ? priceChange : maxRentingPrice.subtract(currentTotalVehiclePrice);
            }
        } else { //max renting time used and exceeded
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal computeFinalPrice(BigDecimal totalPriceForCurrentVehicle){
        return totalPriceForCurrentVehicle.compareTo(minRentingPrice)>=0?totalPriceForCurrentVehicle:minRentingPrice;
    }

    public BigDecimal getPackagePrice() {
        return packagePrice;
    }

    public int getPackageTimeLimitInSeconds() {
        return packageTimeLimitInSeconds;
    }

    public BigDecimal getMinRentingPrice() {
        return minRentingPrice;
    }

    public BigDecimal getDrivingPricePerTimeTickInPackage() {
        return drivingPricePerTimeTickInPackage;
    }

    public BigDecimal getParkingPricePerTimeTickInPackage() {
        return parkingPricePerTimeTickInPackage;
    }

    public BigDecimal getDrivingPricePerTimeTickInPackageExceeded() {
        return drivingPricePerTimeTickInPackageExceeded;
    }

    public BigDecimal getParkingPricePerTimeTickInPackageExceeded() {
        return parkingPricePerTimeTickInPackageExceeded;
    }

    public int getSecondsPerTimeTickInPackage() {
        return secondsPerTimeTickInPackage;
    }

    public int getSecondsPerTimeTickInPackageExceeded() {
        return secondsPerTimeTickInPackageExceeded;
    }

    public BigDecimal getMaxRentingPrice() {
        return maxRentingPrice;
    }

    public boolean isKilometerPriceEnabledAboveMaxRentingPrice() {
        return kilometerPriceEnabledAboveMaxRentingPrice;
    }

    public BigDecimal getStartPrice() {
        return startPrice;
    }

    public int getFreeSeconds() {
        return freeSeconds;
    }

    public BigDecimal getKilometerPrice() {
        return kilometerPrice;
    }
}
