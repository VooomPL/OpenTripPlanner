package org.opentripplanner.hasura_client;

import com.fasterxml.jackson.core.type.TypeReference;
import org.opentripplanner.estimator.hasura_client.hasura_objects.VehicleStateSnapshotHasuraObject;
import org.opentripplanner.hasura_client.mappers.HasuraToOTPMapper;
import org.opentripplanner.hasura_client.mappers.VehicleHistoricalPositionsMapper;
import org.opentripplanner.routing.core.vehicle_sharing.Provider;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleDescription;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.updater.vehicle_sharing.vehicles_positions.SharedVehiclesSnapshotLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.temporal.ChronoUnit;
import java.util.Map;

public class VehicleHistoricalPositionsGetter extends HasuraGetter<VehicleDescription, VehicleStateSnapshotHasuraObject> {

    private static final Logger LOG = LoggerFactory.getLogger(VehicleHistoricalPositionsGetter.class);

    private final Map<Integer, Provider> vehicleProviders;
    private final SharedVehiclesSnapshotLabel snapshotLabel;

    public VehicleHistoricalPositionsGetter(SharedVehiclesSnapshotLabel snapshotLabel, Map<Integer, Provider> vehicleProviders) {
        super(true);
        this.vehicleProviders = vehicleProviders;
        this.snapshotLabel = snapshotLabel;
    }

    @Override
    protected String query() {
        return
                "{\"query\": \"query HistoricalVehiclesForArea($latMin: float8, $lonMin: float8, $latMax: float8, $lonMax: float8, $startTimestamp: timestamptz, $endTimestamp: timestamptz) {\\n" +
                        "  items:vehicles_history_bigint(\\n" +
                        "  where: {\\n" +
                        "      latitude: { _gte: $latMin, _lte: $latMax }\\n" +
                        "      longitude: { _gte: $lonMin, _lte: $lonMax }\\n" +
                        "      createdAt: { _gte: $startTimestamp, _lt: $endTimestamp }\\n" +
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
                "  \"startTimestamp\": \"" + snapshotLabel.getTimestamp() + "\"," +
                "  \"endTimestamp\": \"" + snapshotLabel.getTimestamp().plus(1, ChronoUnit.MINUTES) + "\"" +
                "}}";
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    protected HasuraToOTPMapper<VehicleStateSnapshotHasuraObject, VehicleDescription> mapper() {
        return new VehicleHistoricalPositionsMapper(snapshotLabel, vehicleProviders);
    }

    @Override
    protected TypeReference<ApiResponse<VehicleStateSnapshotHasuraObject>> hasuraType() {
        return new TypeReference<>() {
        };
    }

}
