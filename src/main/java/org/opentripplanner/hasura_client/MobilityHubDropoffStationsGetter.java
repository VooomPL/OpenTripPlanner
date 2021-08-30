package org.opentripplanner.hasura_client;

import com.fasterxml.jackson.core.type.TypeReference;
import org.opentripplanner.hasura_client.hasura_objects.MobilityHubDropoffStationHasura;
import org.opentripplanner.hasura_client.mappers.HasuraToOTPMapper;
import org.opentripplanner.hasura_client.mappers.MobilityHubDropoffStationMapper;
import org.opentripplanner.routing.edgetype.rentedgetype.MobilityHubDropoffStation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MobilityHubDropoffStationsGetter extends HasuraGetter<MobilityHubDropoffStation, MobilityHubDropoffStationHasura> {

    private static final Logger LOG = LoggerFactory.getLogger(MobilityHubDropoffStationsGetter.class);

    @Override
    protected String query() {
        return "{\"query\": \"query getMobilityHubDropoffStations($latMin: float8, $lonMin: float8, $latMax: float8, $lonMax: float8) {\\n" +
                "  items:mobility_hub_dropoff_stations(\\n" +
                "  where: {\\n" +
                "      latitude: { _gte: $latMin, _lte: $latMax }\\n" +
                "      longitude: { _gte: $lonMin, _lte: $lonMax }\\n" +
                "    }\\n" +
                "  ) {\\n" +
                "    latitude\\n" +
                "    longitude\\n" +
                "    vehicleType:vehicle_type\\n" +
                "    providerId:provider_id\\n" +
                "  }\\n" +
                "}\",";
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    protected boolean addAdditionalArguments() {
        return true;
    }

    @Override
    protected HasuraToOTPMapper<MobilityHubDropoffStationHasura, MobilityHubDropoffStation> mapper() {
        return new MobilityHubDropoffStationMapper();
    }

    @Override
    protected TypeReference<ApiResponse<MobilityHubDropoffStationHasura>> hasuraType() {
        return new TypeReference<ApiResponse<MobilityHubDropoffStationHasura>>() {
        };
    }
}
