package org.opentripplanner.hasura_client;

import com.fasterxml.jackson.core.type.TypeReference;
import org.opentripplanner.hasura_client.hasura_objects.Vehicle;
import org.opentripplanner.hasura_client.mappers.HasuraToOTPMapper;
import org.opentripplanner.hasura_client.mappers.VehiclePositionsMapper;
import org.opentripplanner.routing.core.vehicle_sharing.Provider;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VehiclePositionsGetter extends HasuraGetter<VehicleDescription, Vehicle> {
    private static final Logger LOG = LoggerFactory.getLogger(VehiclePositionsGetter.class);

    @Override
    protected String query() {
        return
                "{\"query\": \"query VehiclesForArea($latMin: float8, $lonMin: float8, $latMax: float8, $lonMax: float8) {\\n" +
                        "  items:vehicles(\\n" +
                        "  where: {\\n" +
                        "      latitude: { _gte: $latMin, _lte: $latMax }\\n" +
                        "      longitude: { _gte: $lonMin, _lte: $lonMax }\\n" +
                        "      provider: { available: { _eq: true } }\\n" +
                        "    }\\n" +
                        "  ) {\\n" +
                        "    providerVehicleId\\n" +
                        "    latitude\\n" +
                        "    longitude\\n" +
                        "    fuelType\\n" +
                        "    gearbox\\n" +
                        "    type\\n" +
                        "    range\\n" +
                        "    provider {\\n" +
                        "      providerId: id\\n" +
                        "      providerName: name\\n" +
                        "    }\\n" +
                        "    kmPrice\\n" +
                        "    drivingPrice\\n" +
                        "    startPrice\\n" +
                        "    stopPrice\\n" +
                        "    maxDailyPrice\\n" +
                        "  }\\n" +
                        "}\",";

    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    protected HasuraToOTPMapper<Vehicle, VehicleDescription> mapper() {
        return new VehiclePositionsMapper();
    }

    @Override
    protected TypeReference<ApiResponse<Vehicle>> hasuraType() {
        return new TypeReference<ApiResponse<Vehicle>>() {
        };
    }

    public List<Provider> getResponsiveProviders() {
        return ((VehiclePositionsMapper) mapper()).getNumberOfMappedVehiclesPerProvider().entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(Map.Entry::getKey).collect(Collectors.toList());
    }
}
