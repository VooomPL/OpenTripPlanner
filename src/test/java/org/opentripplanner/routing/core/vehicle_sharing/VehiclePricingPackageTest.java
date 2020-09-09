package org.opentripplanner.routing.core.vehicle_sharing;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class VehiclePricingPackageTest {

    @Test
    public void shouldReturnProperOneKilometerPrice(){
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"));
        assertTrue(car.getActivePackage().computeDistanceAssociatedPriceChange(1200, 800).compareTo(car.getActivePackage().getKilometerPrice()) == 0);
    }

    @Test
    public void shouldReturnZeroKilometerPrice(){
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"));
        assertTrue(car.getActivePackage().computeDistanceAssociatedPriceChange(1200, 799).compareTo(BigDecimal.ZERO) == 0);
    }

    @Test
    public void shouldProperlyInitializeFreeSeconds(){
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"));
        assertEquals(car.getActivePackage().getFreeSeconds(), car.getActivePackage().computeRemainingFreeSeconds(-1, 0), 0.0);
    }

    @Test
    public void shouldReduceFreeSecondsToNonZeroValue(){
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"));
        assertEquals(50, car.getActivePackage().computeRemainingFreeSeconds(100, 50), 0.0);
    }

    @Test
    public void shouldReduceFreeSecondsToZeroValue(){
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"));
        assertEquals(0, car.getActivePackage().computeRemainingFreeSeconds(100, 300), 0.0);
    }

    @Test
    public void shouldReduceTimeByFreeSecondsToNonZeroAndComputeZeroPrice(){
        VehiclePricingPackage pricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 0, 20, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.valueOf(3.0), 1, 1, BigDecimal.ZERO);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), pricingPackage);
        assertTrue(car.getActivePackage().computeTimeAssociatedPriceChange(BigDecimal.ZERO, 15, 50, 10).compareTo(BigDecimal.ZERO)==0);
    }

    @Test
    public void shouldReduceTimeByFreeSecondsToZeroAndComputeZeroPrice(){
        VehiclePricingPackage pricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 0, 20, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(1.5), BigDecimal.ZERO, BigDecimal.valueOf(3.0), 1, 1, BigDecimal.ZERO);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"),pricingPackage);
        assertTrue(car.getActivePackage().computeTimeAssociatedPriceChange(BigDecimal.ZERO, 15, 50, 15).compareTo(BigDecimal.ZERO) == 0);
    }

    @Test
    public void shouldReduceTimeByFreeSecondsToBelowZeroAndComputeProperPriceWithinThePackage(){
        VehiclePricingPackage pricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 80, 20, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.valueOf(1.5), BigDecimal.ZERO, BigDecimal.valueOf(3.0), 1, 1, BigDecimal.ZERO);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), pricingPackage);
        assertTrue(car.getActivePackage().computeTimeAssociatedPriceChange(BigDecimal.ZERO, 15, 50, 20).compareTo(BigDecimal.valueOf(5)) == 0);
   }

    @Test
    public void shouldReduceTimeByFreeSecondsToBelowZeroAndComputeZeroPriceDueToPackagePaymentAtTheBeginning(){
        VehiclePricingPackage pricingPackage = new VehiclePricingPackage(BigDecimal.valueOf(90.0), 80, 20, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(1.5), BigDecimal.ZERO, BigDecimal.valueOf(3.0), 1, 1, BigDecimal.ZERO);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), pricingPackage);
        assertTrue(car.getActivePackage().computeTimeAssociatedPriceChange(BigDecimal.ZERO, 15, 50, 20).compareTo(BigDecimal.ZERO) == 0);
   }

    @Test
    public void shouldReduceTimeByFreeSecondsToBelowZeroAndComputeProperPriceWithinThePackageWhenTwoSecondsTimeTick(){
        VehiclePricingPackage pricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 80, 20, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.valueOf(1.5), BigDecimal.ZERO, BigDecimal.valueOf(3.0), 2, 1, BigDecimal.ZERO);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), pricingPackage);
        //The expected value is higher due to rounding up
        assertTrue(car.getActivePackage().computeTimeAssociatedPriceChange(BigDecimal.ZERO, 15, 50, 20).compareTo(BigDecimal.valueOf(3)) == 0);
    }

    @Test
    public void shouldReduceTimeByFreeSecondsByFreeSecondsToBelowZeroAndComputeProperPriceWhenPackageLimitReached(){
        VehiclePricingPackage pricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 30, 20, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.valueOf(1.5), BigDecimal.ZERO, BigDecimal.valueOf(3.0), 1, 1, BigDecimal.ZERO);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), pricingPackage);
        assertTrue(car.getActivePackage().computeTimeAssociatedPriceChange(BigDecimal.ZERO, 15, 50, 30).compareTo(BigDecimal.valueOf(15)) == 0);
    }

    @Test
    public void shouldNotReduceTimeByFreeSecondsAndComputeProperPriceWhenPackageExceeded(){
        VehiclePricingPackage pricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 30, 20, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.valueOf(1.5), BigDecimal.ZERO, BigDecimal.valueOf(3.0), 1, 1, BigDecimal.ZERO);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), pricingPackage);
        assertTrue(car.getActivePackage().computeTimeAssociatedPriceChange(BigDecimal.ZERO,0, 55, 5).compareTo(BigDecimal.valueOf(7.5)) == 0);
    }

    @Test
    public void shouldNotReduceTimeByFreeSecondsAndComputeProperPriceWhenPackageExceededWhenTwoSecondsTimeTick(){
        VehiclePricingPackage pricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 30, 20, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.valueOf(1.5), BigDecimal.ZERO, BigDecimal.valueOf(3.0), 2, 1, BigDecimal.ZERO);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), pricingPackage);
        //The expected value is higher due to rounding up
        assertTrue(car.getActivePackage().computeTimeAssociatedPriceChange(BigDecimal.ZERO, 0, 50, 5).compareTo(BigDecimal.valueOf(3)) == 0);
    }

    @Test
    public void shouldIncreasePriceExactlyUpToMaxPackagePriceAndReturnProperPriceChange(){
        VehiclePricingPackage pricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 0, 20, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.valueOf(3.0), 2, 1, BigDecimal.valueOf(3));
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), pricingPackage);
        assertTrue(car.getActivePackage().computeTimeAssociatedPriceChange(BigDecimal.ZERO, 0, 50, 2).compareTo(BigDecimal.valueOf(2)) == 0);
    }

    @Test
    public void shouldNotIncreasePriceAboveMaxPackagePriceAndReturnZero(){
        VehiclePricingPackage pricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 0, 20, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.valueOf(3.0), 2, 1, BigDecimal.valueOf(3));
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), pricingPackage);
        assertTrue(car.getActivePackage().computeTimeAssociatedPriceChange(BigDecimal.valueOf(3), 0, 50, 2).compareTo(BigDecimal.ZERO) == 0);
    }

    @Test
    public void shouldNotIncreasePriceAboveMaxPackagePriceAndReturnOnlySecondsToMaxPackagePrice(){
        VehiclePricingPackage pricingPackage = new VehiclePricingPackage(BigDecimal.ZERO, 0, 20, BigDecimal.ZERO, BigDecimal.valueOf(2.0), BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.valueOf(3.0), 2, 1, BigDecimal.valueOf(3));
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), pricingPackage);
        assertTrue(car.getActivePackage().computeTimeAssociatedPriceChange(BigDecimal.valueOf(2), 0, 50, 5).compareTo(BigDecimal.ONE) == 0);
    }


}
