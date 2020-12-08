package org.opentripplanner.hasura_client;

import com.fasterxml.jackson.core.type.TypeReference;
import org.opentripplanner.hasura_client.hasura_objects.AreaForVehicleType;
import org.opentripplanner.hasura_client.mappers.AreaForVehicleTypesMapper;
import org.opentripplanner.hasura_client.mappers.HasuraToOTPMapper;
import org.opentripplanner.updater.vehicle_sharing.parking_zones.GeometriesDisallowedForVehicleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CityGovForbiddenZonesGetter extends HasuraGetter<GeometriesDisallowedForVehicleType, AreaForVehicleType> {

    private static final Logger LOG = LoggerFactory.getLogger(CityGovForbiddenZonesGetter.class);

    @Override
    protected String query() {
        return "{\"query\": \"query GetCityGovForbiddenZones {\\n" +
                "  items:city_gov_forbidden_dropoff_areas {\\n" +
                "    vehicleType:vehicle_type\\n" +
                "    area\\n" +
                "    }\\n" +
                "  }\"" +
                "}";
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
    protected HasuraToOTPMapper<AreaForVehicleType, GeometriesDisallowedForVehicleType> mapper() {
        return new AreaForVehicleTypesMapper();
    }

    @Override
    protected TypeReference<ApiResponse<AreaForVehicleType>> hasuraType() {
        return new TypeReference<ApiResponse<AreaForVehicleType>>() {
        };
    }
}
