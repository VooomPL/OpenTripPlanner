package org.opentripplanner.estimator.hasura_client.mappers;

import org.opentripplanner.estimator.hasura_client.hasura_objects.ProviderHasuraObject;
import org.opentripplanner.hasura_client.mappers.HasuraToOTPMapper;
import org.opentripplanner.routing.core.vehicle_sharing.Provider;

public class ProviderMapper extends HasuraToOTPMapper<ProviderHasuraObject, Provider> {

    @Override
    protected Provider mapSingleHasuraObject(ProviderHasuraObject providerHasuraObject) {
        return new Provider(providerHasuraObject.getId(), providerHasuraObject.getName());
    }
}
