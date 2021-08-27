package org.opentripplanner.estimator.hasura_client.mappers;

import org.opentripplanner.hasura_client.hasura_objects.VehicleHistoricalPositionHasuraObject;
import org.opentripplanner.hasura_client.hasura_objects.Vehicle;
import org.opentripplanner.hasura_client.mappers.HasuraToOTPMapper;
import org.opentripplanner.routing.core.vehicle_sharing.Provider;

import java.util.*;

public class VehicleStateSnapshotMapper extends HasuraToOTPMapper<VehicleHistoricalPositionHasuraObject, Vehicle> {

    private Map<Integer, Provider> vehicleProviders;

    public VehicleStateSnapshotMapper(Map<Integer, Provider> vehicleProviders) {
        this.vehicleProviders = Objects.requireNonNullElse(vehicleProviders, Collections.emptyMap());
    }

    @Override
    protected Vehicle mapSingleHasuraObject(VehicleHistoricalPositionHasuraObject vehicleHistoricalPositionHasuraObject) {
        Provider vehicleProvider = vehicleProviders.get(vehicleHistoricalPositionHasuraObject.getProviderId());
        if (Objects.nonNull(vehicleProvider)) {
            /*This is not actually mapping anything, which is a little bit ugly workaround to incorporate the original
            vehicle getting code here and still being able to serialize the data from the snapshot as a json file for
            Wiremock vehicles database*/
            return vehicleHistoricalPositionHasuraObject.toVehicle(vehicleProvider);
        } else {
            return null;
        }
    }
}
