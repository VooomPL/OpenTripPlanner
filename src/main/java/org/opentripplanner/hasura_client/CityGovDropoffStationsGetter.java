package org.opentripplanner.hasura_client;

import com.fasterxml.jackson.core.type.TypeReference;
import org.opentripplanner.hasura_client.hasura_objects.CityGovDropoffStationHasura;
import org.opentripplanner.hasura_client.mappers.CityGovDropoffStationMapper;
import org.opentripplanner.hasura_client.mappers.HasuraToOTPMapper;
import org.opentripplanner.routing.edgetype.rentedgetype.CityGovDropoffStation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CityGovDropoffStationsGetter extends HasuraGetter<CityGovDropoffStation, CityGovDropoffStationHasura> {

    private static final Logger LOG = LoggerFactory.getLogger(CityGovDropoffStationsGetter.class);

    @Override
    protected String query() {
        return ""; // TODO AdamWiktor VMP-62
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    protected boolean addGeolocationArguments() {
        return true; // TODO AdamWiktor VMP-62
    }

    @Override
    protected HasuraToOTPMapper<CityGovDropoffStationHasura, CityGovDropoffStation> mapper() {
        return new CityGovDropoffStationMapper();
    }

    @Override
    protected TypeReference<ApiResponse<CityGovDropoffStationHasura>> hasuraType() {
        return new TypeReference<ApiResponse<CityGovDropoffStationHasura>>() {
        };
    }
}
