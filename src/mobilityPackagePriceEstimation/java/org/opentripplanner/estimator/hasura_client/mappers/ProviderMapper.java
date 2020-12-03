package org.opentripplanner.estimator.hasura_client.mappers;

import org.opentripplanner.estimator.hasura_client.hasura_objects.ProviderHasuraObject;
import org.opentripplanner.hasura_client.mappers.HasuraToOTPMapper;
import org.opentripplanner.routing.core.vehicle_sharing.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProviderMapper extends HasuraToOTPMapper<ProviderHasuraObject, Provider> {

    private static final Logger LOG = LoggerFactory.getLogger(ProviderMapper.class);

    @Override
    protected Provider mapSingleHasuraObject(ProviderHasuraObject providerHasuraObject) {
        return new Provider(providerHasuraObject.getId(), providerHasuraObject.getName());
    }
}
