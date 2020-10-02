package org.opentripplanner.hasura_client.mappers;

import org.locationtech.jts.geom.Geometry;
import org.opentripplanner.hasura_client.hasura_objects.Area;
import org.opentripplanner.hasura_client.hasura_objects.ParkingZone;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleType;
import org.opentripplanner.updater.vehicle_sharing.parking_zones.GeometryParkingZone;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public class ParkingZonesMapper extends HasuraToOTPMapper<ParkingZone, GeometryParkingZone> {

    private final AreaMapper areaMapper = new AreaMapper();

    private List<Geometry> mapToGeometries(Map<Boolean, List<ParkingZone>> groupedByIsAllowed, boolean allowed) {
        List<Area> areas = groupedByIsAllowed.getOrDefault(allowed, emptyList())
                .stream()
                .map(ParkingZone::getArea)
                .collect(toList());
        return areaMapper.map(areas);
    }

    private GeometryParkingZone mapToGeometryParkingZones(
            Map.Entry<String, List<ParkingZone>> vehicleTypeToParkingZones, int providerId
    ) {
        VehicleType vehicleType = VehicleType.fromDatabaseVehicleType(vehicleTypeToParkingZones.getKey());
        if (vehicleType == null) {
            return null;
        }
        Map<Boolean, List<ParkingZone>> groupedByIsAllowed = vehicleTypeToParkingZones.getValue().stream()
                .collect(groupingBy(ParkingZone::isAllowed));
        return new GeometryParkingZone(providerId, vehicleType, mapToGeometries(groupedByIsAllowed, true),
                mapToGeometries(groupedByIsAllowed, false));
    }

    private List<GeometryParkingZone> mapToGeometryParkingZones(
            Map.Entry<Integer, List<ParkingZone>> providerIdToParkingZones
    ) {
        return providerIdToParkingZones.getValue().stream()
                .collect(groupingBy(ParkingZone::getVehicleType))
                .entrySet()
                .stream()
                .map(vehicleTypeToParkingZones -> mapToGeometryParkingZones(vehicleTypeToParkingZones,
                        providerIdToParkingZones.getKey()))
                .collect(toList());
    }

    @Override
    protected GeometryParkingZone mapSingleHasuraObject(ParkingZone hasuraObject) {
        // This feature requires custom list mapping, we cannot map one parking zone into one geometry parking zone
        throw new NotImplementedException();
    }

    @Override
    public List<GeometryParkingZone> map(List<ParkingZone> parkingZones) {
        return parkingZones.stream()
                .collect(groupingBy(ParkingZone::getProviderId))
                .entrySet()
                .stream()
                .map(this::mapToGeometryParkingZones)
                .flatMap(Collection::stream)
                .collect(toList());
    }
}
