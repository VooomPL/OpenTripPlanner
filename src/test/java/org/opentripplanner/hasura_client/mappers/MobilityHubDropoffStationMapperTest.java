package org.opentripplanner.hasura_client.mappers;

import org.junit.Test;
import org.opentripplanner.hasura_client.hasura_objects.MobilityHubDropoffStationHasura;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleType;
import org.opentripplanner.routing.edgetype.rentedgetype.MobilityHubDropoffStation;
import org.opentripplanner.routing.edgetype.rentedgetype.SingleParkingZone;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MobilityHubDropoffStationMapperTest {

    private final MobilityHubDropoffStationMapper mapper = new MobilityHubDropoffStationMapper();

    @Test
    public void shouldGroupWhileMapping() {
        // given
        List<MobilityHubDropoffStationHasura> hasuraInput = List.of(
                new MobilityHubDropoffStationHasura(1.1, 2.2, "car", 4),
                new MobilityHubDropoffStationHasura(1.1, 2.2, "car", 5),
                new MobilityHubDropoffStationHasura(1.1, 2.2, "un-pedal-scooter", 8),
                new MobilityHubDropoffStationHasura(3.3, 4.4, "scooter", 4)
        );

        // when
        List<MobilityHubDropoffStation> result = mapper.map(hasuraInput);

        // then
        assertEquals(2, result.size());

        MobilityHubDropoffStation singleStation = result.stream().filter(station -> station.getLongitude() == 3.3).findFirst().get();
        assertEquals(new MobilityHubDropoffStation(3.3, 4.4, singletonList(new SingleParkingZone(4, VehicleType.MOTORBIKE))), singleStation);

        MobilityHubDropoffStation multiStation = result.stream().filter(station -> station.getLongitude() == 1.1).findFirst().get();
        assertEquals(1.1, multiStation.getLongitude(), 0.);
        assertEquals(2.2, multiStation.getLatitude(), 0.);
        assertEquals(3, multiStation.getParkingZones().size());
        assertTrue(multiStation.getParkingZones().contains(new SingleParkingZone(4, VehicleType.CAR)));
        assertTrue(multiStation.getParkingZones().contains(new SingleParkingZone(5, VehicleType.CAR)));
        assertTrue(multiStation.getParkingZones().contains(new SingleParkingZone(8, VehicleType.KICKSCOOTER)));
    }
}
