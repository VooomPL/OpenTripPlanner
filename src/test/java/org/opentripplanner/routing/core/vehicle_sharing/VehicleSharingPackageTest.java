package org.opentripplanner.routing.core.vehicle_sharing;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class VehicleSharingPackageTest {

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
        VehicleSharingPackage sharingPackage = new VehicleSharingPackage(BigDecimal.ZERO, 0, 20, BigDecimal.ZERO, new BigDecimal(2.0), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, new BigDecimal(3.0), 1, 1, BigDecimal.ZERO);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), sharingPackage);
        assertTrue(car.getActivePackage().computeTimeAssociatedPriceChange(15, 50, 10).compareTo(BigDecimal.ZERO)==0);
    }

    @Test
    public void shouldReduceTimeByFreeSecondsToZeroAndComputeZeroPrice(){
        VehicleSharingPackage sharingPackage = new VehicleSharingPackage(BigDecimal.ZERO, 0, 20, BigDecimal.ZERO, new BigDecimal(2.0), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal(1.5), BigDecimal.ZERO, new BigDecimal(3.0), 1, 1, BigDecimal.ZERO);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"),sharingPackage);
        assertTrue(car.getActivePackage().computeTimeAssociatedPriceChange(15, 50, 15).compareTo(BigDecimal.ZERO) == 0);
    }

    @Test
    public void shouldReduceTimeByFreeSecondsToBelowZeroAndComputeProperPriceWithinThePackage(){
        VehicleSharingPackage sharingPackage = new VehicleSharingPackage(BigDecimal.ZERO, 80, 20, BigDecimal.ZERO, new BigDecimal(2.0), BigDecimal.ONE, BigDecimal.ZERO, new BigDecimal(1.5), BigDecimal.ZERO, new BigDecimal(3.0), 1, 1, BigDecimal.ZERO);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), sharingPackage);
        assertTrue(car.getActivePackage().computeTimeAssociatedPriceChange(15, 50, 20).compareTo(new BigDecimal(5)) == 0);
   }

    @Test
    public void shouldReduceTimeByFreeSecondsToBelowZeroAndComputeZeroPriceDueToPackagePaymentAtTheBeginning(){
        VehicleSharingPackage sharingPackage = new VehicleSharingPackage(new BigDecimal(90.0), 80, 20, BigDecimal.ZERO, new BigDecimal(2.0), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal(1.5), BigDecimal.ZERO, new BigDecimal(3.0), 1, 1, BigDecimal.ZERO);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), sharingPackage);
        assertTrue(car.getActivePackage().computeTimeAssociatedPriceChange(15, 50, 20).compareTo(BigDecimal.ZERO) == 0);
   }

    @Test
    public void shouldReduceTimeByFreeSecondsToBelowZeroAndComputeProperPriceWithinThePackageWhenTwoSecondsTimeTick(){
        VehicleSharingPackage sharingPackage = new VehicleSharingPackage(BigDecimal.ZERO, 80, 20, BigDecimal.ZERO, new BigDecimal(2.0), BigDecimal.ONE, BigDecimal.ZERO, new BigDecimal(1.5), BigDecimal.ZERO, new BigDecimal(3.0), 2, 1, BigDecimal.ZERO);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), sharingPackage);
        assertTrue(car.getActivePackage().computeTimeAssociatedPriceChange(15, 50, 20).compareTo(new BigDecimal(2.5)) == 0);
    }

    @Test
    public void shouldReduceTimeByFreeSecondsByFreeSecondsToBelowZeroAndComputeProperPriceWhenPackageLimitReached(){
        VehicleSharingPackage sharingPackage = new VehicleSharingPackage(BigDecimal.ZERO, 30, 20, BigDecimal.ZERO, new BigDecimal(2.0), BigDecimal.ONE, BigDecimal.ZERO, new BigDecimal(1.5), BigDecimal.ZERO, new BigDecimal(3.0), 1, 1, BigDecimal.ZERO);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), sharingPackage);
        assertTrue(car.getActivePackage().computeTimeAssociatedPriceChange(15, 50, 30).compareTo(new BigDecimal(15)) == 0);
    }

    @Test
    public void shouldNotReduceTimeByFreeSecondsAndComputeProperPriceWhenPackageExceeded(){
        VehicleSharingPackage sharingPackage = new VehicleSharingPackage(BigDecimal.ZERO, 30, 20, BigDecimal.ZERO, new BigDecimal(2.0), BigDecimal.ONE, BigDecimal.ZERO, new BigDecimal(1.5), BigDecimal.ZERO, new BigDecimal(3.0), 1, 1, BigDecimal.ZERO);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), sharingPackage);
        assertTrue(car.getActivePackage().computeTimeAssociatedPriceChange(0, 55, 5).compareTo(new BigDecimal(7.5)) == 0);
    }

    @Test
    public void shouldNotReduceTimeByFreeSecondsAndComputeProperPriceWhenPackageExceededWhenTwoSecondsTimeTick(){
        VehicleSharingPackage sharingPackage = new VehicleSharingPackage(BigDecimal.ZERO, 30, 20, BigDecimal.ZERO, new BigDecimal(2.0), BigDecimal.ONE, BigDecimal.ZERO, new BigDecimal(1.5), BigDecimal.ZERO, new BigDecimal(3.0), 2, 1, BigDecimal.ZERO);
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"), sharingPackage);
        assertTrue(car.getActivePackage().computeTimeAssociatedPriceChange(0, 50, 5).compareTo(new BigDecimal(2.5)) == 0);
    }
}
