package org.opentripplanner.estimator.hasura_client.mappers;

import org.opentripplanner.estimator.hasura_client.hasura_objects.VehicleStateSnapshotHasuraObject;
import org.opentripplanner.hasura_client.hasura_objects.Vehicle;
import org.opentripplanner.hasura_client.mappers.HasuraToOTPMapper;
import org.opentripplanner.hasura_client.mappers.VehiclePositionsMapper;
import org.opentripplanner.routing.core.vehicle_sharing.Provider;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class VehicleStateSnapshotMapper extends HasuraToOTPMapper<VehicleStateSnapshotHasuraObject, VehicleDescription> {

    private static final Logger LOG = LoggerFactory.getLogger(VehicleStateSnapshotMapper.class);

    private Map<Integer, Provider> vehicleProviders;

    public VehicleStateSnapshotMapper(Map<Integer, Provider> vehicleProviders) {
        this.vehicleProviders = vehicleProviders;
    }

    @Override
    protected VehicleDescription mapSingleHasuraObject(VehicleStateSnapshotHasuraObject vehicleStateSnapshotHasuraObject) {
        VehiclePositionsMapper originalVehicleMapper = new VehiclePositionsMapper();
        Provider vehicleProvider = vehicleProviders.get(vehicleStateSnapshotHasuraObject.getProviderId());
        if (Objects.nonNull(vehicleProvider)) {
            //TODO: this seems to be a little bit ugly workaround to avoid copying the original mapping method code here...
            Vehicle stateSnapshotAsVehicle = vehicleStateSnapshotHasuraObject.toVehicle(vehicleProvider);
            List<VehicleDescription> mappedVehicles = originalVehicleMapper.map(Arrays.asList(stateSnapshotAsVehicle));
            return mappedVehicles.get(0);
        } else {
            return null;
        }
    }
}
