package org.opentripplanner.routing.core.vehicle_sharing;

import java.math.BigDecimal;

public class VehiclePricingPackage {

    private BigDecimal packagePrice;

    private int packageTimeLimitInSeconds;

    private int freeSeconds;

    private BigDecimal minRentingPrice;

    private BigDecimal startPrice;

    private BigDecimal drivingPricePerTimeTickInPackage;

    private BigDecimal parkingPricePerTimeTickInPackage;

    private BigDecimal drivingPricePerTimeTickInPackageExceeded;

    private BigDecimal parkingPricePerTimeTickInPackageExceeded;

    private BigDecimal kilometerPrice;

    private int secondsPerTimeTickInPackage;

    private int secondsPerTimeTickInPackageExceeded;

    private BigDecimal maxRentingPrice;

    private boolean kilometerPriceEnabledAboveMaxRentingPrice;

    public VehiclePricingPackage(){
        /* By default creating a "no predefined package" configuration
         * (package time limit is set to 0, so we only use the package exceeded properties to compute the price)
         */
        this(BigDecimal.ZERO, 0, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 60, 60, BigDecimal.ZERO, false);
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

    public void setPackagePrice(BigDecimal packagePrice) {
        this.packagePrice = packagePrice;
    }

    public void setPackageTimeLimitInSeconds(int packageTimeLimitInSeconds) {
        this.packageTimeLimitInSeconds = packageTimeLimitInSeconds;
    }

    public void setFreeSeconds(int freeSeconds) {
        this.freeSeconds = freeSeconds;
    }

    public void setMinRentingPrice(BigDecimal minRentingPrice) {
        this.minRentingPrice = minRentingPrice;
    }

    public void setStartPrice(BigDecimal startPrice) {
        this.startPrice = startPrice;
    }

    public void setDrivingPricePerTimeTickInPackage(BigDecimal drivingPricePerTimeTickInPackage) {
        this.drivingPricePerTimeTickInPackage = drivingPricePerTimeTickInPackage;
    }

    public void setParkingPricePerTimeTickInPackage(BigDecimal parkingPricePerTimeTickInPackage) {
        this.parkingPricePerTimeTickInPackage = parkingPricePerTimeTickInPackage;
    }

    public void setDrivingPricePerTimeTickInPackageExceeded(BigDecimal drivingPricePerTimeTickInPackageExceeded) {
        this.drivingPricePerTimeTickInPackageExceeded = drivingPricePerTimeTickInPackageExceeded;
    }

    public void setParkingPricePerTimeTickInPackageExceeded(BigDecimal parkingPricePerTimeTickInPackageExceeded) {
        this.parkingPricePerTimeTickInPackageExceeded = parkingPricePerTimeTickInPackageExceeded;
    }

    public void setKilometerPrice(BigDecimal kilometerPrice) {
        this.kilometerPrice = kilometerPrice;
    }

    public void setSecondsPerTimeTickInPackage(int secondsPerTimeTickInPackage) {
        this.secondsPerTimeTickInPackage = secondsPerTimeTickInPackage;
    }

    public void setSecondsPerTimeTickInPackageExceeded(int secondsPerTimeTickInPackageExceeded) {
        this.secondsPerTimeTickInPackageExceeded = secondsPerTimeTickInPackageExceeded;
    }

    public void setMaxRentingPrice(BigDecimal maxRentingPrice) {
        this.maxRentingPrice = maxRentingPrice;
    }

    public void setKilometerPriceEnabledAboveMaxRentingPrice(boolean kilometerPriceEnabledAboveMaxRentingPrice) {
        this.kilometerPriceEnabledAboveMaxRentingPrice = kilometerPriceEnabledAboveMaxRentingPrice;
    }
}
