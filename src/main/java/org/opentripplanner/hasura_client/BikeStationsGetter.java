package org.opentripplanner.hasura_client;

import com.fasterxml.jackson.core.type.TypeReference;
import org.opentripplanner.hasura_client.hasura_objects.BikeStationHasura;
import org.opentripplanner.hasura_client.mappers.BikeStationsMapper;
import org.opentripplanner.hasura_client.mappers.HasuraToOTPMapper;
import org.opentripplanner.routing.bike_rental.BikeRentalStation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BikeStationsGetter extends HasuraGetter<BikeRentalStation, BikeStationHasura> {

    private static final Logger LOG = LoggerFactory.getLogger(BikeStationsGetter.class);

    @Override
    protected String query() {
        return "{\"query\": \"query Stations(\\n" +
                "  $latMin: float8\\n" +
                "  $lonMin: float8\\n" +
                "  $latMax: float8\\n" +
                "  $lonMax: float8\\n" +
                ") {\\n" +
                "  items:stations(\\n" +
                "    where: {\\n" +
                "      latitude: { _gte: $latMin, _lte: $latMax }\\n" +
                "      longitude: { _gte: $lonMin, _lte: $lonMax }\\n" +
                "      provider: { available: { _eq: true } }\\n" +
                "    }\\n" +
                "  ) {\\n" +
                "    providerStationId \\n" +
                "    name: displayName\\n" +
                "    provider {\\n" +
                "      providerId: id\\n" +
                "      providerName: name \\n" +
                "    }\\n" +
                "    latitude\\n" +
                "    longitude\\n" +
                "    bikesAvailable: adultBike\\n" +
                "    spacesAvailable: bikeRacks\\n" +
                "  }\\n" +
                "}\",";
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    protected HasuraToOTPMapper<BikeStationHasura, BikeRentalStation> mapper() {
        return new BikeStationsMapper();
    }

    @Override
    protected TypeReference<ApiResponse<BikeStationHasura>> hasuraType() {
        return new TypeReference<ApiResponse<BikeStationHasura>>() {
        };
    }
}
