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
        return "{\"query\": \"query getCityGovDropoffStations($latMin: float8, $lonMin: float8, $latMax: float8, $lonMax: float8) {\\n" +
                "  items:city_gov_dropoff_stations(\\n" +
                "  where: {\\n" +
                "      latitude: { _gte: $latMin, _lte: $latMax }\\n" +
                "      longitude: { _gte: $lonMin, _lte: $lonMax }\\n" +
                "    }\\n" +
                "  ) {\\n" +
                "    latitude\\n" +
                "    longitude\\n" +
                "    vehicleType:vehicle_type\\n" +
                "  }\\n" +
                "}\",";
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    protected boolean addGeolocationArguments() {
        return true;
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
