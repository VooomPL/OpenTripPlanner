package org.opentripplanner.hasura_client.mappers;

import org.opentripplanner.estimator.hasura_client.hasura_objects.VehicleStateSnapshotHasuraObject;
import org.opentripplanner.hasura_client.hasura_objects.Vehicle;
import org.opentripplanner.routing.core.vehicle_sharing.*;
import org.opentripplanner.updater.vehicle_sharing.vehicles_positions.SharedVehiclesSnapshotLabel;

import java.util.*;

//TODO: Paulina Adamska VMP-239 Modify the simulator to use this approach instead of the Wiremock-based-API one
public class VehicleHistoricalPositionsMapper extends HasuraToOTPMapper<VehicleStateSnapshotHasuraObject, VehicleDescription> {

    private final VehiclePositionsMapper toVehicleDescriptionMapper;
    private final Map<Integer, Provider> vehicleProviders;

    public VehicleHistoricalPositionsMapper(SharedVehiclesSnapshotLabel snapshotLabel, Map<Integer, Provider> vehicleProviders) {
        this.toVehicleDescriptionMapper = new VehiclePositionsMapper(snapshotLabel);
        this.vehicleProviders = Objects.requireNonNullElse(vehicleProviders, Collections.emptyMap());
    }

    @Override
    protected VehicleDescription mapSingleHasuraObject(VehicleStateSnapshotHasuraObject vehicleStateSnapshotHasuraObject) {
        Provider vehicleProvider = vehicleProviders.get(vehicleStateSnapshotHasuraObject.getProviderId());
        if (Objects.nonNull(vehicleProvider)) {
            Vehicle vehicleWithProvider = vehicleStateSnapshotHasuraObject.toVehicle(vehicleProvider);
            return toVehicleDescriptionMapper.mapSingleHasuraObject(vehicleWithProvider);
        }
        return null;
    }

}
