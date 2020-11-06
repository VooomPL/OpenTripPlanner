package org.opentripplanner.updater.vehicle_sharing.vehicle_presence;

import com.fasterxml.jackson.databind.JsonNode;
import org.opentripplanner.hasura_client.VehiclePresenceGetter;
import org.opentripplanner.hasura_client.hasura_objects.VehiclePresence;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.updater.GraphUpdaterManager;
import org.opentripplanner.updater.PollingGraphUpdater;
import org.opentripplanner.updater.vehicle_sharing.vehicles_positions.SharedVehiclesUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class VehiclePresencePredictionUpdater extends PollingGraphUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(SharedVehiclesUpdater.class);

    private final VehiclePresenceGetter vehiclePresenceGetter = new VehiclePresenceGetter();
    private GraphUpdaterManager graphUpdaterManager;
    private Graph graph;
    private String url;

    @Override
    protected void runPolling() {
        LOG.info("Polling Vehicle Presence Prediction from API");
        List<VehiclePresence> vehiclePresenceHeatmaps = vehiclePresenceGetter.getFromHasura(graph, url);
        LOG.info("Got vehicle presence map");
        graphUpdaterManager.execute(new VehiclePresenceGraphWriterRunnable(vehiclePresenceHeatmaps));

    }

    @Override
    protected void configurePolling(Graph graph, JsonNode config) throws Exception {
        this.pollingPeriodSeconds = 60;
        this.url = System.getProperty("predictionApiUrl");
        if (this.url == null) {
            throw new IllegalStateException("Please provide program parameter `--predictionApiUrl <URL>`");
        }
    }

    @Override
    public void configure(Graph graph, JsonNode config) throws Exception {
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
