package org.opentripplanner.routing.edgetype.rentedgetype;

import org.junit.Test;
import org.opentripplanner.routing.core.vehicle_sharing.KickScooterDescription;
import org.opentripplanner.routing.core.vehicle_sharing.Provider;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleType;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ParkingZoneInfoTest {

    private static final Provider PROVIDER = new Provider(1, "HopCity");
    private static final KickScooterDescription KICKSCOOTER = new KickScooterDescription("id", 1.0, 2.0, null, null, PROVIDER);
    private static final SingleParkingZone HOPCITY_KICKSCOOTER = new SingleParkingZone(1, VehicleType.KICKSCOOTER);
    private static final SingleParkingZone PANEK_CAR = new SingleParkingZone(2, VehicleType.CAR);

    @Test
    public void shouldAllowDropoffWhenNoParkingZones() {
        // given
        ParkingZoneInfo parkingZoneInfo = new ParkingZoneInfo(emptyList(), emptyList(), emptyList());

        // then
        assertTrue(parkingZoneInfo.canDropoffVehicleHere(KICKSCOOTER));
    }

    @Test
    public void shouldNotAllowDropoffWhenOutsideOfProviderParkingZone() {
        // given
        ParkingZoneInfo parkingZoneInfo = new ParkingZoneInfo(emptyList(), singletonList(HOPCITY_KICKSCOOTER), emptyList());

        // then
        assertFalse(parkingZoneInfo.canDropoffVehicleHere(KICKSCOOTER));
    }

    @Test
    public void shouldAllowDropoffWhenInsideProviderParkingZone() {
        // given
        ParkingZoneInfo parkingZoneInfo = new ParkingZoneInfo(singletonList(HOPCITY_KICKSCOOTER), singletonList(HOPCITY_KICKSCOOTER), emptyList());

        // then
        assertTrue(parkingZoneInfo.canDropoffVehicleHere(KICKSCOOTER));
    }

    @Test
    public void shouldAllowDropoffWhenProviderHasDisabledParkingZones() {
        // given
        ParkingZoneInfo parkingZoneInfo = new ParkingZoneInfo(emptyList(), singletonList(PANEK_CAR), emptyList());

        // then
        assertTrue(parkingZoneInfo.canDropoffVehicleHere(KICKSCOOTER));
    }

    @Test
    public void shouldNotAllowDropoffWhenOutsideOfCityGovernmentParkingZone() {
        // given
        ParkingZoneInfo parkingZoneInfo = new ParkingZoneInfo(singletonList(HOPCITY_KICKSCOOTER), singletonList(HOPCITY_KICKSCOOTER), singletonList(VehicleType.KICKSCOOTER));

        // then
        assertFalse(parkingZoneInfo.canDropoffVehicleHere(KICKSCOOTER));
    }
}
