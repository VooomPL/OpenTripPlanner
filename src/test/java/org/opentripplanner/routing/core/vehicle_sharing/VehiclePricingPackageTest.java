package org.opentripplanner.routing.core.vehicle_sharing;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class VehiclePricingPackageTest {

    @Test
    public void shouldReturnProperOneKilometerPrice(){
        VehiclePricingPackage vehiclePricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 0, 0, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(1.0), BigDecimal.ZERO, BigDecimal.valueOf(3.0), 1, 1, BigDecimal.ZERO, false);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), null, vehiclePricingPackage);
        assertTrue(car.getActivePackage().computeDistanceAssociatedPriceChange(BigDecimal.ZERO, 1200, 800).compareTo(car.getActivePackage().getKilometerPrice()) == 0);
    }

    @Test
    public void shouldReturnZeroKilometerPrice(){
        VehiclePricingPackage vehiclePricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 0, 0, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(1.0), BigDecimal.ZERO, BigDecimal.valueOf(3.0), 1, 1, BigDecimal.ZERO, false);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), null, vehiclePricingPackage);
        assertTrue(car.getActivePackage().computeDistanceAssociatedPriceChange(BigDecimal.ZERO, 1200, 799).compareTo(BigDecimal.ZERO) == 0);
    }

    @Test
    public void shouldReturnZeroKilometerPriceDueToMaxRentingPrice(){
        VehiclePricingPackage vehiclePricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 0, 0, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(1.0), BigDecimal.ZERO, BigDecimal.valueOf(3.0), 1, 1, BigDecimal.valueOf(3), false);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), null, vehiclePricingPackage);
        assertTrue(car.getActivePackage().computeDistanceAssociatedPriceChange(BigDecimal.valueOf(3), 2200, 800).compareTo(BigDecimal.ZERO) == 0);
    }

    @Test
    public void shouldReturnKilometerPriceFractionDueToMaxRentingPrice(){
        VehiclePricingPackage vehiclePricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 0, 0, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(1.0), BigDecimal.ZERO, BigDecimal.valueOf(3.0), 1, 1, BigDecimal.valueOf(3), false);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), null, vehiclePricingPackage);
        assertTrue(car.getActivePackage().computeDistanceAssociatedPriceChange(BigDecimal.ONE, 1000, 3000).compareTo(BigDecimal.valueOf(2)) == 0);
    }

    @Test
    public void shouldReturnFullKilometerPriceDueToMaxRentingPriceEnabledAboveMaxRentingPrice(){
        VehiclePricingPackage vehiclePricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 0, 0, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(1.0), BigDecimal.ZERO, BigDecimal.valueOf(3.0), 1, 1, BigDecimal.valueOf(3), true);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), null, vehiclePricingPackage);
        assertTrue(car.getActivePackage().computeDistanceAssociatedPriceChange(BigDecimal.ONE, 1000, 3000).compareTo(BigDecimal.valueOf(9)) == 0);
    }

    @Test
    public void shouldReduceFreeSecondsToNonZeroValue(){
        VehiclePricingPackage vehiclePricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 0, 0, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(1.0), BigDecimal.ZERO, BigDecimal.valueOf(3.0), 1, 1, BigDecimal.ZERO, false);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), null, vehiclePricingPackage);
        assertEquals(50, car.getActivePackage().computeRemainingFreeSeconds(100, 50), 0.0);
    }

    @Test
    public void shouldReduceFreeSecondsToZeroValue(){
        VehiclePricingPackage vehiclePricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 0, 0, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(1.0), BigDecimal.ZERO, BigDecimal.valueOf(3.0), 1, 1, BigDecimal.ZERO, false);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), null, vehiclePricingPackage);
        assertEquals(0, car.getActivePackage().computeRemainingFreeSeconds(100, 300), 0.0);
    }

    @Test
    public void shouldReduceTimeByFreeSecondsToNonZeroAndComputeZeroPrice(){
        VehiclePricingPackage pricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 0, 20, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.valueOf(3.0), 1, 1, BigDecimal.ZERO, false);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), null, pricingPackage);
        assertTrue(car.getActivePackage().computeTimeAssociatedPriceChange(BigDecimal.ZERO, 15, 50, 10).compareTo(BigDecimal.ZERO)==0);
    }

    @Test
    public void shouldReduceTimeByFreeSecondsToZeroAndComputeZeroPrice(){
        VehiclePricingPackage pricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 0, 20, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(1.5), BigDecimal.ZERO, BigDecimal.valueOf(3.0), 1, 1, BigDecimal.ZERO, false);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), null, pricingPackage);
        assertTrue(car.getActivePackage().computeTimeAssociatedPriceChange(BigDecimal.ZERO, 15, 50, 15).compareTo(BigDecimal.ZERO) == 0);
    }

    @Test
    public void shouldReduceTimeByFreeSecondsToBelowZeroAndComputeProperPriceWithinThePackage(){
        VehiclePricingPackage pricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 80, 20, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.valueOf(1.5), BigDecimal.ZERO, BigDecimal.valueOf(3.0), 1, 1, BigDecimal.ZERO, false);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), null, pricingPackage);
        assertTrue(car.getActivePackage().computeTimeAssociatedPriceChange(BigDecimal.ZERO, 15, 50, 20).compareTo(BigDecimal.valueOf(5)) == 0);
   }

    @Test
    public void shouldReduceTimeByFreeSecondsToBelowZeroAndComputeZeroPriceDueToPackagePaymentAtTheBeginning(){
        VehiclePricingPackage pricingPackage = new VehiclePricingPackage(BigDecimal.valueOf(90.0), 80, 20, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(1.5), BigDecimal.ZERO, BigDecimal.valueOf(3.0), 1, 1, BigDecimal.ZERO, false);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), null, pricingPackage);
        assertTrue(car.getActivePackage().computeTimeAssociatedPriceChange(BigDecimal.ZERO, 15, 50, 20).compareTo(BigDecimal.ZERO) == 0);
   }

    @Test
    public void shouldReduceTimeByFreeSecondsToBelowZeroAndComputeProperPriceWithinThePackageWhenTwoSecondsTimeTick(){
        VehiclePricingPackage pricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 80, 20, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.valueOf(1.5), BigDecimal.ZERO, BigDecimal.valueOf(3.0), 2, 1, BigDecimal.ZERO, false);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), null, pricingPackage);
        assertTrue(car.getActivePackage().computeTimeAssociatedPriceChange(BigDecimal.ZERO, 15, 50, 20).compareTo(BigDecimal.valueOf(2.5)) == 0);
    }

    @Test
    public void shouldReduceTimeByFreeSecondsByFreeSecondsToBelowZeroAndComputeProperPriceWhenPackageLimitReached(){
        VehiclePricingPackage pricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 30, 20, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.valueOf(1.5), BigDecimal.ZERO, BigDecimal.valueOf(3.0), 1, 1, BigDecimal.ZERO, false);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), null, pricingPackage);
        assertTrue(car.getActivePackage().computeTimeAssociatedPriceChange(BigDecimal.ZERO, 15, 50, 30).compareTo(BigDecimal.valueOf(15)) == 0);
    }

    @Test
    public void shouldNotReduceTimeByFreeSecondsAndComputeProperPriceWhenPackageExceeded(){
        VehiclePricingPackage pricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 30, 20, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.valueOf(1.5), BigDecimal.ZERO, BigDecimal.valueOf(3.0), 1, 1, BigDecimal.ZERO, false);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), null, pricingPackage);
        assertTrue(car.getActivePackage().computeTimeAssociatedPriceChange(BigDecimal.ZERO,0, 55, 5).compareTo(BigDecimal.valueOf(7.5)) == 0);
    }

    @Test
    public void shouldNotReduceTimeByFreeSecondsAndComputeProperPriceWhenPackageExceededWhenTwoSecondsTimeTick(){
        VehiclePricingPackage pricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 30, 20, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.valueOf(1.5), BigDecimal.ZERO, BigDecimal.valueOf(3.0), 2, 1, BigDecimal.ZERO, false);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), null, pricingPackage);
        assertTrue(car.getActivePackage().computeTimeAssociatedPriceChange(BigDecimal.ZERO, 0, 50, 5).compareTo(BigDecimal.valueOf(2.5)) == 0);
    }

    @Test
    public void shouldIncreasePriceExactlyUpToMaxPackagePriceAndReturnProperPriceChange(){
        VehiclePricingPackage pricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 0, 20, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.valueOf(3.0), 2, 1, BigDecimal.valueOf(3), false);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), null, pricingPackage);
        assertTrue(car.getActivePackage().computeTimeAssociatedPriceChange(BigDecimal.ZERO, 0, 50, 2).compareTo(BigDecimal.valueOf(2)) == 0);
    }

    @Test
    public void shouldNotIncreasePriceAboveMaxPackagePriceAndReturnZero(){
        VehiclePricingPackage pricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 0, 20, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.valueOf(3.0), 2, 1, BigDecimal.valueOf(3), false);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), null, pricingPackage);
        assertTrue(car.getActivePackage().computeTimeAssociatedPriceChange(BigDecimal.valueOf(3), 0, 50, 2).compareTo(BigDecimal.ZERO) == 0);
    }

    @Test
    public void shouldNotIncreasePriceAboveMaxPackagePriceAndReturnOnlySecondsToMaxPackagePrice(){
        VehiclePricingPackage pricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 0, 20, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.valueOf(3.0), 2, 1, BigDecimal.valueOf(3), false);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), null, pricingPackage);
        assertTrue(car.getActivePackage().computeTimeAssociatedPriceChange(BigDecimal.valueOf(2), 0, 50, 5).compareTo(BigDecimal.ONE) == 0);
    }

    @Test
    public void shouldCountPartOfTimeChangeAsInPackageAndSomeAsPackageExceeded(){
        VehiclePricingPackage pricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 5, 5, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ZERO, BigDecimal.ZERO, 1, 1, BigDecimal.ZERO, false);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), null, pricingPackage);
        assertTrue(car.getActivePackage().computeTimeAssociatedPriceChange(BigDecimal.ZERO, 1, 12, 8).compareTo(BigDecimal.valueOf(9)) == 0);
    }

    @Test
    public void shouldCountPartOfTimeChangeAsInPackageAndSomeAsPackageExceededWithoutFreeSeconds(){
        VehiclePricingPackage pricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 5, 5, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ZERO, BigDecimal.ZERO, 1, 1, BigDecimal.ZERO, false);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), null, pricingPackage);
        assertTrue(car.getActivePackage().computeTimeAssociatedPriceChange(BigDecimal.ZERO, 0, 12, 7).compareTo(BigDecimal.valueOf(9)) == 0);
    }

    @Test
    public void shouldCountLastSecondOfPackageAsInPackageAndRestAsAbovePackage(){
        VehiclePricingPackage pricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 5, 5, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ZERO, BigDecimal.ZERO, 1, 1, BigDecimal.ZERO, false);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), null, pricingPackage);
        assertTrue(car.getActivePackage().computeTimeAssociatedPriceChange(BigDecimal.ZERO, 0, 20, 11).compareTo(BigDecimal.valueOf(21)) == 0);
    }

    @Test
    public void shouldEntireTimeAsPackageExceeded(){
        VehiclePricingPackage pricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 5, 5, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ZERO, BigDecimal.ZERO, 1, 1, BigDecimal.ZERO, false);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), null, pricingPackage);
        assertTrue(car.getActivePackage().computeTimeAssociatedPriceChange(BigDecimal.ZERO, 0, 20, 10).compareTo(BigDecimal.valueOf(20)) == 0);
    }



}
