package org.opentripplanner.updater.vehicle_sharing.vehicle_presence;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import org.opentripplanner.prediction_client.VehiclePresence;
import org.opentripplanner.prediction_client.VehiclePresenceGetter;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.updater.GraphUpdaterManager;
import org.opentripplanner.updater.PollingGraphUpdater;
import org.opentripplanner.updater.vehicle_sharing.vehicles_positions.SharedVehiclesUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

public class VehiclePresencePredictionUpdater extends PollingGraphUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(SharedVehiclesUpdater.class);
    private static final Map<String, String> params = ImmutableMap.of("vehicle", "CAR");

    private final VehiclePresenceGetter vehiclePresenceGetter = new VehiclePresenceGetter();
    private GraphUpdaterManager graphUpdaterManager;
    private Graph graph;
    private String url;

    @Override
    protected void runPolling() {
        LOG.info("Polling Vehicle Presence Prediction from API");
        Optional<VehiclePresence> vehiclePresenceHeatmap = vehiclePresenceGetter.getPrediction(url, params);
        LOG.info("Got vehicle presence map");
        vehiclePresenceHeatmap.ifPresent(it -> graphUpdaterManager.execute(new VehiclePresenceGraphWriterRunnable(it)));
    }

    @Override
    protected void configurePolling(Graph graph, JsonNode config) {
        this.pollingPeriodSeconds = 600;
        this.url = System.getProperty("predictionApiUrl");
        if (this.url == null) {
            throw new IllegalStateException("Please provide program parameter `--predictionApiUrl <URL>`");
        }
    }

    @Override
    public void configure(Graph graph, JsonNode config) {
        configurePolling(graph, config);
        type = "Vehicle presence prediction";
    }

    @Override
    public void setGraphUpdaterManager(GraphUpdaterManager updaterManager) {
        this.graphUpdaterManager = updaterManager;
    }

    @Override
    public void setup(Graph graph) {
        graph.carPresencePredictor = new CarPresencePredictor();
        this.graph = graph;
    }

    @Override
    public void teardown() {

    }
}
