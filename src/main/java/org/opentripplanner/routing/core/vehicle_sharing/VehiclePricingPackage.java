package org.opentripplanner.routing.core.vehicle_sharing;

import java.math.BigDecimal;

public class VehiclePricingPackage {

    private final BigDecimal packagePrice;

    //Change to timeLimit collection for scenarios like: : for the first 47 min you pay X per minute, and after that, you pay Y per hour, after 4 hours you pay the same amount as for 23 hours?
    private final int packageTimeLimitInSeconds;

    private final int freeSeconds;

    private final BigDecimal minRentingPrice;

    private final BigDecimal startPrice;

    //Necessary for scenarios like: for the first 47 min you pay X per minute, and after that, you pay Y per hour
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
        this(BigDecimal.ZERO, 0, 0, /*BigDecimal.valueOf(1000.59)*/BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(1.0), BigDecimal.ZERO, BigDecimal.valueOf(3.0), 1, 1, BigDecimal.TEN, false);
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
                    priceChange = (BigDecimal.valueOf(timeChangeInSeconds).divide(BigDecimal.valueOf(secondsPerTimeTickInPackage), BigDecimal.ROUND_HALF_UP)).multiply(drivingPricePerTimeTickInPackage);
                } else {
                    priceChange = (BigDecimal.valueOf(timeChangeInSeconds).divide(BigDecimal.valueOf(secondsPerTimeTickInPackageExceeded), BigDecimal.ROUND_HALF_UP)).multiply(drivingPricePerTimeTickInPackageExceeded);
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
