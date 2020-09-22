package org.opentripplanner.routing.core.vehicle_sharing;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class VehiclePricingPackageTest {

    @Test
    public void shouldReturnProperOneKilometerPrice() {
        VehiclePricingPackage vehiclePricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 0, 0, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(1.0), BigDecimal.ZERO, BigDecimal.valueOf(3.0), 1, 1, BigDecimal.ZERO, false);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), null, vehiclePricingPackage);
        BigDecimal timePrice = BigDecimal.ZERO;
        BigDecimal distancePrice =  BigDecimal.ZERO;
        BigDecimal startPrice =  BigDecimal.ZERO;
        assertTrue(car.getActivePackage().computeDistanceAssociatedPrice(startPrice, timePrice, distancePrice, 1200).compareTo(car.getActivePackage().getKilometerPrice()) == 0);
    }

    @Test
    public void shouldReturnZeroKilometerPrice(){
        VehiclePricingPackage vehiclePricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 0, 0, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(1.0), BigDecimal.ZERO, BigDecimal.valueOf(3.0), 1, 1, BigDecimal.ZERO, false);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), null, vehiclePricingPackage);
        BigDecimal timePrice = BigDecimal.ZERO;
        BigDecimal distancePrice =  BigDecimal.ZERO;
        BigDecimal startPrice =  BigDecimal.ZERO;
        assertTrue(car.getActivePackage().computeDistanceAssociatedPrice(startPrice, timePrice, distancePrice, 799).compareTo(BigDecimal.ZERO) == 0);
    }

    @Test
    public void shouldReturnFractionOfKilometerPriceDueToMaxRentingPrice(){
        VehiclePricingPackage vehiclePricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 0, 0, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(1.0), BigDecimal.ZERO, BigDecimal.valueOf(3.0), 1, 1, BigDecimal.valueOf(4), false);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), null, vehiclePricingPackage);
        BigDecimal timePrice = BigDecimal.ONE;
        BigDecimal distancePrice =  BigDecimal.ONE;
        BigDecimal startPrice =  BigDecimal.ONE;
        assertTrue(car.getActivePackage().computeDistanceAssociatedPrice(startPrice, timePrice, distancePrice, 4200).compareTo(BigDecimal.valueOf(2)) == 0);
    }

    @Test
    public void shouldReturnZeroKilometerPriceDueToMaxRentingPriceAndOtherPriceCategories(){
        VehiclePricingPackage vehiclePricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 0, 0, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(1.0), BigDecimal.ZERO, BigDecimal.valueOf(3.0), 1, 1, BigDecimal.valueOf(2), false);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), null, vehiclePricingPackage);
        BigDecimal timePrice = BigDecimal.ONE;
        BigDecimal distancePrice =  BigDecimal.ZERO;
        BigDecimal startPrice =  BigDecimal.ONE;
        assertTrue(car.getActivePackage().computeDistanceAssociatedPrice(startPrice, timePrice, distancePrice, 2200).compareTo(BigDecimal.ZERO) == 0);
    }

    @Test
    public void shouldReturnFractionOfKilometerPriceDueToMaxRentingPriceAndOtherPriceCategories(){
        VehiclePricingPackage vehiclePricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 0, 0, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(1.0), BigDecimal.ZERO, BigDecimal.valueOf(3.0), 1, 1, BigDecimal.valueOf(3), false);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), null, vehiclePricingPackage);
        BigDecimal timePrice = BigDecimal.ONE;
        BigDecimal distancePrice =  BigDecimal.ZERO;
        BigDecimal startPrice =  BigDecimal.ONE;
        assertTrue(car.getActivePackage().computeDistanceAssociatedPrice(startPrice, timePrice, distancePrice, 2200).compareTo(BigDecimal.ONE) == 0);
    }

    @Test
    public void shouldReturnFullKilometerPriceDueToMaxRentingPriceEnabledAboveMaxRentingPrice(){
        VehiclePricingPackage vehiclePricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 0, 0, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(1.0), BigDecimal.ZERO, BigDecimal.valueOf(3.0), 1, 1, BigDecimal.valueOf(3), true);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), null, vehiclePricingPackage);
        BigDecimal timePrice = BigDecimal.ONE;
        BigDecimal distancePrice =  BigDecimal.ONE;
        BigDecimal startPrice =  BigDecimal.ONE;
        assertTrue(car.getActivePackage().computeDistanceAssociatedPrice(startPrice, timePrice, distancePrice, 4200).compareTo(BigDecimal.valueOf(12)) == 0);
    }

    @Test
    public void shouldReduceTimeByFreeSecondsToNonZeroAndComputeZeroPrice(){
        VehiclePricingPackage pricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 0, 20, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.valueOf(3.0), 1, 1, BigDecimal.ZERO, false);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), null, pricingPackage);
        BigDecimal timePrice = BigDecimal.ONE;
        BigDecimal distancePrice =  BigDecimal.ONE;
        BigDecimal startPrice =  BigDecimal.ONE;
        assertTrue(car.getActivePackage().computeTimeAssociatedPrice(startPrice, timePrice, distancePrice, 15).compareTo(BigDecimal.ZERO)==0);
    }

    @Test
    public void shouldReduceTimeByFreeSecondsToBelowZeroAndComputeZeroPriceDueToPackagePaymentAtTheBeginning(){
        VehiclePricingPackage pricingPackage = new VehiclePricingPackage(BigDecimal.valueOf(90.0), 80, 20, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(1.5), BigDecimal.ZERO, BigDecimal.valueOf(3.0), 1, 1, BigDecimal.ZERO, false);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), null, pricingPackage);
        BigDecimal timePrice = BigDecimal.ZERO;
        BigDecimal distancePrice =  BigDecimal.ZERO;
        BigDecimal startPrice =  BigDecimal.valueOf(90); /*package price*/
        assertTrue(car.getActivePackage().computeTimeAssociatedPrice(startPrice, timePrice, distancePrice, 30).compareTo(BigDecimal.ZERO) == 0);
    }

    @Test
    public void shouldComputeProperPriceWithinAndAbovePackageWhenOneMinuteTimeTickInAndOneHourTimeTickAbovePackage(){
        VehiclePricingPackage pricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 80, 20, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.valueOf(0.65), BigDecimal.ZERO, BigDecimal.valueOf(1.5), BigDecimal.ZERO, BigDecimal.valueOf(3.0), 60, 3600, BigDecimal.ZERO, false);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), null, pricingPackage);
        BigDecimal timePrice = BigDecimal.ZERO;
        BigDecimal distancePrice =  BigDecimal.ZERO;
        BigDecimal startPrice =  BigDecimal.ZERO;
        //used 5 seconds of non free seconds - started 1st time tick:
        assertTrue(car.getActivePackage().computeTimeAssociatedPrice(startPrice, timePrice, distancePrice, 25).compareTo(BigDecimal.valueOf(0.65)) == 0);
        //completed previously counted time tick - next one not started yet:
        assertTrue(car.getActivePackage().computeTimeAssociatedPrice(startPrice, timePrice, distancePrice, 80).compareTo(BigDecimal.valueOf(0.65)) == 0);
        //started next time tick:
        assertTrue(car.getActivePackage().computeTimeAssociatedPrice(startPrice, timePrice, distancePrice, 82).compareTo(BigDecimal.valueOf(1.3)) == 0);
        //finished package but the last minute within package has not finished yet:
        assertTrue(car.getActivePackage().computeTimeAssociatedPrice(startPrice, timePrice, distancePrice, 100).compareTo(BigDecimal.valueOf(1.3)) == 0);
        //started first time tick above package:
        assertTrue(car.getActivePackage().computeTimeAssociatedPrice(startPrice, timePrice, distancePrice, 141).compareTo(BigDecimal.valueOf(2.8)) == 0);
        //it is still only first time tick above package due to time tick length change:
        assertTrue(car.getActivePackage().computeTimeAssociatedPrice(startPrice, timePrice, distancePrice, 3500).compareTo(BigDecimal.valueOf(2.8)) == 0);
    }

    @Test
    public void shouldComputeProperPriceAbovePackageWhenPackageNotUsedAndOneMinuteTimeTickAbovePackage(){
        VehiclePricingPackage pricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 0, 20, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(1.5), BigDecimal.ZERO, BigDecimal.valueOf(3.0), 60, 60, BigDecimal.ZERO, false);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), null, pricingPackage);
        BigDecimal timePrice = BigDecimal.ZERO;
        BigDecimal distancePrice =  BigDecimal.ZERO;
        BigDecimal startPrice =  BigDecimal.ZERO;
        //used 5 seconds of non free seconds - started 1st time tick:
        assertTrue(car.getActivePackage().computeTimeAssociatedPrice(startPrice, timePrice, distancePrice, 25).compareTo(BigDecimal.valueOf(1.5)) == 0);
        //completed previously counted time tick - next one not started yet:
        assertTrue(car.getActivePackage().computeTimeAssociatedPrice(startPrice, timePrice, distancePrice, 80).compareTo(BigDecimal.valueOf(1.5)) == 0);
        //started next time tick:
        assertTrue(car.getActivePackage().computeTimeAssociatedPrice(startPrice, timePrice, distancePrice, 82).compareTo(BigDecimal.valueOf(3)) == 0);
    }

    @Test
    public void shouldReturnPreviousPriceDueToMaxPriceAlreadyReached(){
        VehiclePricingPackage pricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 0, 20, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(1.5), BigDecimal.ZERO, BigDecimal.valueOf(3.0), 60, 60, BigDecimal.valueOf(3), false);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), null, pricingPackage);
        BigDecimal timePrice = BigDecimal.valueOf(2);
        BigDecimal distancePrice =  BigDecimal.valueOf(0.5);
        BigDecimal startPrice =  BigDecimal.valueOf(0.5);
        //returning previous price due to max price already reached
        assertTrue(car.getActivePackage().computeTimeAssociatedPrice(startPrice, timePrice, distancePrice, 50).compareTo(BigDecimal.valueOf(2)) == 0);
    }

    @Test
    public void shouldReturnFractionOfActualTimeAssociatedPriceDueToMaxPriceExceededAfterPriceIncrement(){
        VehiclePricingPackage pricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 0, 20, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(1.5), BigDecimal.ZERO, BigDecimal.valueOf(3.0), 60, 60, BigDecimal.valueOf(3), false);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), null, pricingPackage);
        BigDecimal timePrice = BigDecimal.valueOf(1);
        BigDecimal distancePrice =  BigDecimal.valueOf(0.5);
        BigDecimal startPrice =  BigDecimal.valueOf(0.5);
        //returning previous price due to max price already reached:
        assertTrue(car.getActivePackage().computeTimeAssociatedPrice(startPrice, timePrice, distancePrice, 200).compareTo(BigDecimal.valueOf(2)) == 0);
    }

    @Test
    public void shouldReturnEntireActualTimeAssociatedPriceDueToMaxPriceNotExceededAfterPriceIncrement(){
        VehiclePricingPackage pricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 0, 20, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(1.5), BigDecimal.ZERO, BigDecimal.valueOf(3.0), 60, 60, BigDecimal.valueOf(20), false);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), null, pricingPackage);
        BigDecimal timePrice = BigDecimal.valueOf(1);
        BigDecimal distancePrice =  BigDecimal.valueOf(0.5);
        BigDecimal startPrice =  BigDecimal.valueOf(0.5);
        //returning previous price due to max price already reached:
        assertTrue(car.getActivePackage().computeTimeAssociatedPrice(startPrice, timePrice, distancePrice, 200).compareTo(BigDecimal.valueOf(4.5)) == 0);
    }
}
