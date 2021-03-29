package org.opentripplanner.estimator.hasura_client.mappers;

import org.opentripplanner.estimator.hasura_client.hasura_objects.VehicleStateSnapshotHasuraObject;
import org.opentripplanner.hasura_client.hasura_objects.Vehicle;
import org.opentripplanner.hasura_client.mappers.HasuraToOTPMapper;
import org.opentripplanner.routing.core.vehicle_sharing.Provider;

import java.util.*;

public class VehicleStateSnapshotMapper extends HasuraToOTPMapper<VehicleStateSnapshotHasuraObject, Vehicle> {

    private Map<Integer, Provider> vehicleProviders;

    public VehicleStateSnapshotMapper(Map<Integer, Provider> vehicleProviders) {
        this.vehicleProviders = Objects.requireNonNullElse(vehicleProviders, Collections.emptyMap());
    }

    @Override
    protected Vehicle mapSingleHasuraObject(VehicleStateSnapshotHasuraObject vehicleStateSnapshotHasuraObject) {
        Provider vehicleProvider = vehicleProviders.get(vehicleStateSnapshotHasuraObject.getProviderId());
        if (Objects.nonNull(vehicleProvider)) {
            /*This is not actually mapping anything, which is a little bit ugly workaround to incorporate the original
            vehicle getting code here and still being able to serialize the data from the snapshot as a json file for
            Wiremock vehicles database*/
            return vehicleStateSnapshotHasuraObject.toVehicle(vehicleProvider);
        } else {
            return null;
        }
    }
}
