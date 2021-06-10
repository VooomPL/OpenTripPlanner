package org.opentripplanner.estimator.hasura_client;

import com.fasterxml.jackson.core.type.TypeReference;
import org.opentripplanner.estimator.hasura_client.hasura_objects.ProviderHasuraObject;
import org.opentripplanner.estimator.hasura_client.mappers.ProviderMapper;
import org.opentripplanner.hasura_client.ApiResponse;
import org.opentripplanner.hasura_client.HasuraGetter;
import org.opentripplanner.hasura_client.mappers.HasuraToOTPMapper;
import org.opentripplanner.routing.core.vehicle_sharing.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//TODO: Paulina Adamska VMP-239 Move this from simulator-associated to the main OTP hasura_client package
public class ProvidersGetter extends HasuraGetter<Provider, ProviderHasuraObject> {

    private static final Logger LOG = LoggerFactory.getLogger(ProvidersGetter.class);

    @Override
    protected String query() {
        return "{\"query\": \"{ items:providers { id, name } }\"}";
    }

    protected boolean addAdditionalArguments() {
        return false;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    protected HasuraToOTPMapper<ProviderHasuraObject, Provider> mapper() {
        return new ProviderMapper();
    }

    @Override
    protected TypeReference<ApiResponse<ProviderHasuraObject>> hasuraType() {
        return new TypeReference<ApiResponse<ProviderHasuraObject>>() {
        };
    }
}
