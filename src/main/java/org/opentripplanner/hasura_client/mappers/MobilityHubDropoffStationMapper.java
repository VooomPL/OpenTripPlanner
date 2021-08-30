package org.opentripplanner.hasura_client.mappers;

import org.locationtech.jts.geom.Coordinate;
import org.opentripplanner.hasura_client.hasura_objects.MobilityHubDropoffStationHasura;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleType;
import org.opentripplanner.routing.edgetype.rentedgetype.MobilityHubDropoffStation;
import org.opentripplanner.routing.edgetype.rentedgetype.SingleParkingZone;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class MobilityHubDropoffStationMapper extends HasuraToOTPMapper<MobilityHubDropoffStationHasura, MobilityHubDropoffStation> {

    @Override
    protected MobilityHubDropoffStation mapSingleHasuraObject(MobilityHubDropoffStationHasura station) {
        // This feature requires custom list mapping
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MobilityHubDropoffStation> map(List<MobilityHubDropoffStationHasura> stations) {
        return stations.stream()
                .collect(Collectors.groupingBy(station -> new Coordinate(station.getLongitude(), station.getLatitude())))
                .entrySet()
                .stream()
                .map(entry -> new MobilityHubDropoffStation(entry.getKey().x, entry.getKey().y, mapToParkingZones(entry.getValue())))
                .collect(toList());
    }

    private List<SingleParkingZone> mapToParkingZones(List<MobilityHubDropoffStationHasura> stations) {
        return stations.stream()
                .map(station -> {
                    VehicleType vehicleType = VehicleType.fromDatabaseVehicleType(station.getVehicleType());
                    if (vehicleType == null) {
                        return null;
                    }
                    return new SingleParkingZone(station.getProviderId(), vehicleType);
                })
                .filter(Objects::nonNull)
                .distinct()
                .collect(toList());
    }
}
