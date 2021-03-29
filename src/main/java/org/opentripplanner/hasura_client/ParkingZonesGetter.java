package org.opentripplanner.hasura_client;

import com.fasterxml.jackson.core.type.TypeReference;
import org.opentripplanner.hasura_client.hasura_objects.ParkingZone;
import org.opentripplanner.hasura_client.mappers.HasuraToOTPMapper;
import org.opentripplanner.hasura_client.mappers.ParkingZonesMapper;
import org.opentripplanner.updater.vehicle_sharing.parking_zones.GeometryParkingZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParkingZonesGetter extends HasuraGetter<GeometryParkingZone, ParkingZone> {

    private static final Logger LOG = LoggerFactory.getLogger(ParkingZonesGetter.class);

    @Override
    protected String query() {
        return "{\"query\": \"query GetParkingZones($latMin: float8, $lonMin: float8, $latMax: float8, $lonMax: float8) {" +
                "  items:parking_zones(\\n" +
                "  where: {\\n" +
                "      _and: [\\n" +
                "        {\\n" +
                "          _or: [\\n" +
                "            { longSW: { _gte: $lonMin, _lte: $lonMax } }\\n" +
                "            { longNE: { _gte: $lonMin, _lte: $lonMax } }\\n" +
                "            { longSW: { _lte: $lonMin }, longNE: { _gte: $lonMin } }\\n" +
                "          ]\\n" +
                "        }\\n" +
                "        {\\n" +
                "          _or: [\\n" +
                "            { latSW: { _gte: $latMin, _lte: $latMax } }\\n" +
                "            { latNE: { _gte: $latMin, _lte: $latMax } }\\n" +
                "            { latSW: { _lte: $latMin }, latNE: { _gte: $latMin } }\\n" +
                "          ]\\n" +
                "        }\\n" +
                "      ]\\n" +
                "    }\\n" +
                "  ) {\\n" +
                "    providerId\\n" +
                "    vehicleType\\n" +
                "    isAllowed\\n" +
                "    area\\n" +
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
    protected HasuraToOTPMapper<ParkingZone, GeometryParkingZone> mapper() {
        return new ParkingZonesMapper();
    }

    @Override
    protected TypeReference<ApiResponse<ParkingZone>> hasuraType() {
        return new TypeReference<ApiResponse<ParkingZone>>() {
        };
    }
}
