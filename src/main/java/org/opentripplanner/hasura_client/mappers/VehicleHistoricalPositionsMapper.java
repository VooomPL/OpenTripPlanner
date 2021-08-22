package org.opentripplanner.hasura_client.mappers;

import org.opentripplanner.hasura_client.hasura_objects.VehicleHistoricalPositionHasuraObject;
import org.opentripplanner.hasura_client.hasura_objects.Vehicle;
import org.opentripplanner.routing.core.vehicle_sharing.*;
import org.opentripplanner.updater.vehicle_sharing.vehicles_positions.SharedVehiclesSnapshotLabel;

import java.util.*;

public class VehicleHistoricalPositionsMapper extends HasuraToOTPMapper<VehicleHistoricalPositionHasuraObject, VehicleDescription> {

    private final VehiclePositionsMapper toVehicleDescriptionMapper;
    private final Map<Integer, Provider> vehicleProviders;

    public VehicleHistoricalPositionsMapper(SharedVehiclesSnapshotLabel snapshotLabel, Map<Integer, Provider> vehicleProviders) {
        this.toVehicleDescriptionMapper = new VehiclePositionsMapper(snapshotLabel);
        this.vehicleProviders = Objects.requireNonNullElse(vehicleProviders, Collections.emptyMap());
    }

    @Override
    protected VehicleDescription mapSingleHasuraObject(VehicleHistoricalPositionHasuraObject vehicleHistoricalPositionHasuraObject) {
        Provider vehicleProvider = vehicleProviders.get(vehicleHistoricalPositionHasuraObject.getProviderId());
        if (Objects.nonNull(vehicleProvider)) {
            Vehicle vehicleWithProvider = vehicleHistoricalPositionHasuraObject.toVehicle(vehicleProvider);
            return toVehicleDescriptionMapper.mapSingleHasuraObject(vehicleWithProvider);
        }
        return null;
    }

}
