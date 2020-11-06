package org.opentripplanner.hasura_client;

import com.fasterxml.jackson.core.type.TypeReference;
import org.opentripplanner.hasura_client.hasura_objects.VehiclePresence;
import org.opentripplanner.hasura_client.mappers.HasuraToOTPMapper;
import org.opentripplanner.hasura_client.mappers.VehiclePresenceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VehiclePresenceGetter extends HasuraGetter<VehiclePresence, VehiclePresence> {

    private static final Logger LOG = LoggerFactory.getLogger(HasuraGetter.class);

    @Override
    protected String query() {
        return "";
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    protected boolean addGeolocationArguments() {
        return false;
    }

    @Override
    protected HasuraToOTPMapper<VehiclePresence, VehiclePresence> mapper() {
        return new VehiclePresenceMapper();
    }

    @Override
    protected TypeReference<ApiResponse<VehiclePresence>> hasuraType() {
        return new TypeReference<>() {
        };
    }
}
