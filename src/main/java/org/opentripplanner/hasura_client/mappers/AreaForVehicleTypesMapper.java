package org.opentripplanner.hasura_client.mappers;

import org.opentripplanner.hasura_client.hasura_objects.Area;
import org.opentripplanner.hasura_client.hasura_objects.AreaForVehicleType;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleType;
import org.opentripplanner.updater.vehicle_sharing.parking_zones.GeometriesDisallowedForVehicleType;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public class AreaForVehicleTypesMapper extends HasuraToOTPMapper<AreaForVehicleType, GeometriesDisallowedForVehicleType> {

    private final AreaMapper areaMapper = new AreaMapper();

    private GeometriesDisallowedForVehicleType mapToGeometriesDisallowed(
            Map.Entry<String, List<AreaForVehicleType>> vehicleTypeToParkingZones
    ) {
        VehicleType vehicleType = VehicleType.fromDatabaseVehicleType(vehicleTypeToParkingZones.getKey());
        if (vehicleType == null) {
            return null;
        }
        List<Area> areasDisallowed = vehicleTypeToParkingZones.getValue()
                .stream()
                .map(AreaForVehicleType::getArea)
                .collect(toList());
        return new GeometriesDisallowedForVehicleType(vehicleType, areaMapper.map(areasDisallowed));
    }

    @Override
    protected GeometriesDisallowedForVehicleType mapSingleHasuraObject(AreaForVehicleType areaForVehicleType) {
        // This feature requires custom list mapping, we cannot map one parking zone into one geometry parking zone
        throw new NotImplementedException();
    }

    @Override
    public List<GeometriesDisallowedForVehicleType> map(List<AreaForVehicleType> areasForVehicleTypes) {
        return areasForVehicleTypes.stream()
                .collect(groupingBy(AreaForVehicleType::getVehicleType))
                .entrySet()
                .stream()
                .map(this::mapToGeometriesDisallowed)
                .filter(Objects::nonNull)
                .collect(toList());
    }
}
