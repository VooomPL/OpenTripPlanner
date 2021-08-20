package org.opentripplanner.hasura_client;

import com.fasterxml.jackson.core.type.TypeReference;
import org.opentripplanner.hasura_client.hasura_objects.ParkingZone;
import org.opentripplanner.hasura_client.mappers.HasuraToOTPMapper;
import org.opentripplanner.hasura_client.mappers.ParkingZonesMapper;
import org.opentripplanner.updater.vehicle_sharing.parking_zones.GeometryParkingZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MobilityHubForbiddenZonesGetter extends HasuraGetter<GeometryParkingZone, ParkingZone> {

    private static final Logger LOG = LoggerFactory.getLogger(MobilityHubForbiddenZonesGetter.class);

    @Override
    protected String query() {
        return "{\"query\": \"query GetMobilityHubForbiddenZones {\\n" +
                "  items:mobility_hub_forbidden_dropoff_areas {\\n" +
                "    vehicleType:vehicle_type\\n" +
                "    providerId:provider_id\\n" +
                "    area\\n" +
                "    isAllowed:is_allowed\\n" +
                "    }\\n" +
                "  }\"" +
                "}";
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    protected boolean addAdditionalArguments() {
        return false;
    }

    @Override
    protected HasuraToOTPMapper<ParkingZone, GeometryParkingZone> mapper() {
        return new ParkingZonesMapper();
    }

    @Override
    protected TypeReference<ApiResponse<ParkingZone>> hasuraType() {
        return new TypeReference<ApiResponse<ParkingZone>>() {
        };
    }
}
