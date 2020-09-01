package org.opentripplanner.hasura_client;

import com.fasterxml.jackson.core.type.TypeReference;
import org.opentripplanner.hasura_client.hasura_objects.BikeStationHasura;
import org.opentripplanner.hasura_client.mappers.BikeStationsMapper;
import org.opentripplanner.hasura_client.mappers.HasuraToOTPMapper;
import org.opentripplanner.routing.bike_rental.BikeRentalStation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BikeStationsGetter extends HasuraGetter<BikeRentalStation, BikeStationHasura> {

    @Override
    protected String QUERY() {
        return null;
    }

    @Override
    protected HasuraToOTPMapper<BikeStationHasura, BikeRentalStation> mapper() {
        return new BikeStationsMapper();
    }

    @Override
    protected TypeReference<ApiResponse<BikeStationHasura>> hasuraType() {
        return new TypeReference<ApiResponse<BikeStationHasura>>() {};
    }
}
