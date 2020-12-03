package org.opentripplanner.estimator.hasura_client;

import com.fasterxml.jackson.core.type.TypeReference;
import org.opentripplanner.estimator.hasura_client.hasura_objects.VehicleStateSnapshotHasuraObject;
import org.opentripplanner.estimator.hasura_client.mappers.VehicleStateSnapshotMapper;
import org.opentripplanner.hasura_client.ApiResponse;
import org.opentripplanner.hasura_client.HasuraGetter;
import org.opentripplanner.hasura_client.mappers.HasuraToOTPMapper;
import org.opentripplanner.routing.core.vehicle_sharing.Provider;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleDescription;
import org.opentripplanner.routing.graph.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

public class VehicleStateSnapshotGetter extends HasuraGetter<VehicleDescription, VehicleStateSnapshotHasuraObject> {

    private static final Logger LOG = LoggerFactory.getLogger(VehicleStateSnapshotGetter.class);

    private Map<Integer, Provider> vehicleProviders;
    private LocalDateTime snapshotTimestamp;

    public VehicleStateSnapshotGetter(Map<Integer, Provider> vehicleProviders, LocalDateTime snapshotTimestamp) {
        this.vehicleProviders = vehicleProviders;
        this.snapshotTimestamp = snapshotTimestamp;
    }

    @Override
    protected String query() {
        return
                "{\"query\": \"query HistoricalVehiclesForArea($latMin: float8, $lonMin: float8, $latMax: float8, $lonMax: float8, $startTimestamp: timestamptz, $endTimestamp: timestamptz) {\\n" +
                        "  items:vehicles_history_bigint(\\n" +
                        "  where: {\\n" +
                        "      latitude: { _gte: $latMin, _lte: $latMax }\\n" +
                        "      longitude: { _gte: $lonMin, _lte: $lonMax }\\n" +
                        "      updatedAt: { _gte: $startTimestamp, _lt: $endTimestamp }\\n" +
                        "    }\\n" +
                        "  ) {\\n" +
                        "    providerVehicleId\\n" +
                        "    latitude\\n" +
                        "    longitude\\n" +
                        "    fuelType\\n" +
                        "    gearbox\\n" +
                        "    type\\n" +
                        "    range\\n" +
                        "    providerId\\n" +
                        "    kmPrice\\n" +
                        "    drivingPrice\\n" +
                        "    startPrice\\n" +
                        "    stopPrice\\n" +
                        "    maxDailyPrice\\n" +
                        "  }\\n" +
                        "}\",";
    }

    @Override
    protected String getAdditionalArguments(Graph graph) {
        double latMin = graph.getOsmEnvelope().getLowerLeftLatitude();
        double lonMin = graph.getOsmEnvelope().getLowerLeftLongitude();
        double latMax = graph.getOsmEnvelope().getUpperRightLatitude();
        double lonMax = graph.getOsmEnvelope().getUpperRightLongitude();

        return "\"variables\": {" +
                "  \"latMin\": " + latMin + "," +
                "  \"lonMin\": " + lonMin + "," +
                "  \"latMax\": " + latMax + "," +
                "  \"lonMax\": " + lonMax + "," +
                "  \"startTimestamp\": \"" + snapshotTimestamp + "\"," +
                "  \"endTimestamp\": \"" + snapshotTimestamp.plus(1, ChronoUnit.MINUTES) + "\"" +
                "}}";
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    protected HasuraToOTPMapper<VehicleStateSnapshotHasuraObject, VehicleDescription> mapper() {
        return new VehicleStateSnapshotMapper(vehicleProviders);
    }

    @Override
    protected TypeReference<ApiResponse<VehicleStateSnapshotHasuraObject>> hasuraType() {
        return new TypeReference<ApiResponse<VehicleStateSnapshotHasuraObject>>() {
        };
    }

}
