package org.opentripplanner.routing.core.vehicle_sharing;

import java.math.BigDecimal;
import java.math.RoundingMode;

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

    public VehiclePricingPackage() {
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
        this.secondsPerTimeTickInPackage = secondsPerTimeTickInPackage > 0 ? secondsPerTimeTickInPackage : 1;
        this.secondsPerTimeTickInPackageExceeded = secondsPerTimeTickInPackageExceeded > 0 ? secondsPerTimeTickInPackageExceeded : 1;
        this.maxRentingPrice = maxRentingPrice;
        this.kilometerPriceEnabledAboveMaxRentingPrice = kilometerPriceEnabledAboveMaxRentingPrice;
    }

    public BigDecimal computeStartPrice() {
        return packagePrice.add(startPrice);
    }

    public BigDecimal computeTimeAssociatedPrice(BigDecimal currentStartPrice, BigDecimal currentTimePrice, BigDecimal currentDistancePrice, int totalDrivingTimeInSeconds) {
        BigDecimal previousTotalPrice = currentStartPrice.add(currentTimePrice).add(currentDistancePrice);
        if (!isMaxRentingPriceUsed() || !isMaxRentingPriceExceeded(previousTotalPrice)) {
            BigDecimal newTimeAssociatedPrice = BigDecimal.ZERO;

            totalDrivingTimeInSeconds -= freeSeconds;

            if (totalDrivingTimeInSeconds > 0) { //not all seconds of travel were free of charge
                //Computing price associated with time ticks in package
                int secondsInPackage = Math.min(totalDrivingTimeInSeconds, packageTimeLimitInSeconds);
                BigDecimal timeTicksInPackage = BigDecimal.valueOf(secondsInPackage).divide(BigDecimal.valueOf(secondsPerTimeTickInPackage), 0, RoundingMode.UP);
                newTimeAssociatedPrice = timeTicksInPackage.multiply(drivingPricePerTimeTickInPackage);

                //Computing price associated with time ticks above package
                int secondsAbovePackage = totalDrivingTimeInSeconds - timeTicksInPackage.intValue() * secondsPerTimeTickInPackage;
                if (secondsAbovePackage > 0) {
                    BigDecimal timeTicksAbovePackage = BigDecimal.valueOf(secondsAbovePackage).divide(BigDecimal.valueOf(secondsPerTimeTickInPackageExceeded), 0, RoundingMode.UP);
                    newTimeAssociatedPrice = newTimeAssociatedPrice.add(timeTicksAbovePackage.multiply(drivingPricePerTimeTickInPackageExceeded));
                }
            }
            if (!isMaxRentingPriceUsed()) { //max renting time not used, no need to check anything else
                return newTimeAssociatedPrice;
            } else { //max renting price used, check whether it is going to be exceeded after adding price change
                BigDecimal newTotalPrice = previousTotalPrice.subtract(currentTimePrice)
                        .add(newTimeAssociatedPrice);
                return isMaxRentingPriceExceeded(newTotalPrice) ? maxRentingPrice.subtract(
                        previousTotalPrice.subtract(currentTimePrice))
                        : newTimeAssociatedPrice;
            }
        } else { //max renting price used and exceeded - do not increase price at this point
            return currentTimePrice;
        }
    }

    private boolean isMaxRentingPriceUsed() {
        return maxRentingPrice.compareTo(BigDecimal.ZERO) > 0;
    }

    private boolean isMaxRentingPriceExceeded(BigDecimal totalVehiclePrice) {
        return totalVehiclePrice.compareTo(maxRentingPrice) >= 0;
    }

    public BigDecimal computeDistanceAssociatedPrice(BigDecimal currentStartPrice, BigDecimal currentTimePrice, BigDecimal currentDistancePrice, double totalDistanceInMeters) {
        BigDecimal previousTotalPrice = currentStartPrice.add(currentTimePrice).add(currentDistancePrice);
        if (!isMaxRentingPriceUsed() || !isMaxRentingPriceExceeded(previousTotalPrice) ||
                kilometerPriceEnabledAboveMaxRentingPrice) {
            int newDistanceInKilometers = (int) (totalDistanceInMeters / 1000);
            BigDecimal newDistancePrice = new BigDecimal(newDistanceInKilometers).multiply(kilometerPrice);
            if (!isMaxRentingPriceUsed() || kilometerPriceEnabledAboveMaxRentingPrice) {
                // max renting price not used or counting full kilometer price anyway
                return newDistancePrice;
            } else { //max renting price used, check whether it is going to be exceeded after adding price change
                BigDecimal newTotalPrice = previousTotalPrice.subtract(currentDistancePrice)
                        .add(newDistancePrice);
                return isMaxRentingPriceExceeded(newTotalPrice) ? maxRentingPrice.subtract(
                        previousTotalPrice.subtract(currentDistancePrice))
                        : newDistancePrice;
            }
        } else { //max renting time used and exceeded
            return currentDistancePrice;
        }
    }

    public BigDecimal computeFinalPrice(BigDecimal totalPriceForCurrentVehicle) {
        return totalPriceForCurrentVehicle.compareTo(minRentingPrice) >= 0 ? totalPriceForCurrentVehicle : minRentingPrice;
    }

    public BigDecimal computeTotalPrice(int totalTimeInSeconds, int totalDistanceInMeters) {
        BigDecimal startPrice = computeStartPrice();
        BigDecimal timePrice = computeTimeAssociatedPrice(startPrice, BigDecimal.ZERO, BigDecimal.ZERO, totalTimeInSeconds);
        BigDecimal distancePrice = computeDistanceAssociatedPrice(startPrice, timePrice, BigDecimal.ZERO, totalDistanceInMeters);

        return computeFinalPrice(startPrice.add(timePrice).add(distancePrice));
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
