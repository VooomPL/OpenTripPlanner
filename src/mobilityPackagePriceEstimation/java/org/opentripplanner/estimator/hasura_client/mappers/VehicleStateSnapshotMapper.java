package org.opentripplanner.estimator.hasura_client.mappers;

import org.opentripplanner.estimator.hasura_client.hasura_objects.VehicleStateSnapshotHasuraObject;
import org.opentripplanner.hasura_client.hasura_objects.Vehicle;
import org.opentripplanner.hasura_client.mappers.HasuraToOTPMapper;
import org.opentripplanner.hasura_client.mappers.VehiclePositionsMapper;
import org.opentripplanner.routing.core.vehicle_sharing.Provider;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleDescription;

import java.util.*;

public class VehicleStateSnapshotMapper extends HasuraToOTPMapper<VehicleStateSnapshotHasuraObject, VehicleDescription> {

    private Map<Integer, Provider> vehicleProviders;

    public VehicleStateSnapshotMapper(Map<Integer, Provider> vehicleProviders) {
        this.vehicleProviders = Objects.requireNonNullElse(vehicleProviders, Collections.emptyMap());
    }

    @Override
    protected VehicleDescription mapSingleHasuraObject(VehicleStateSnapshotHasuraObject vehicleStateSnapshotHasuraObject) {
        VehiclePositionsMapper originalVehicleMapper = new VehiclePositionsMapper();
        Provider vehicleProvider = vehicleProviders.get(vehicleStateSnapshotHasuraObject.getProviderId());
        if (Objects.nonNull(vehicleProvider)) {
            /*This is a little bit ugly workaround to automatically incorporate all future modifications of the original
            vehicle mapping method code here, without changing the existing vehicle mapper interface*/
            Vehicle stateSnapshotAsVehicle = vehicleStateSnapshotHasuraObject.toVehicle(vehicleProvider);
            List<VehicleDescription> mappedVehicles = originalVehicleMapper.map(Arrays.asList(stateSnapshotAsVehicle));
            return mappedVehicles.get(0);
        } else {
            return null;
        }
    }
}
