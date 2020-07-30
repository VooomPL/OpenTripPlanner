package org.opentripplanner.updater.vehicle_sharing.vehicles_positions;

import com.fasterxml.jackson.databind.JsonNode;
import org.opentripplanner.graph_builder.linking.RentableVehiclesLinker;
import org.opentripplanner.graph_builder.linking.TemporaryStreetSplitter;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.updater.GraphUpdaterManager;
import org.opentripplanner.updater.PollingGraphUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SharedVehiclesUpdater extends PollingGraphUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(SharedVehiclesUpdater.class);

    private final VehiclePositionsGetter vehiclePositionsGetter = new VehiclePositionsGetter();
    private RentableVehiclesLinker rentableVehiclesLinker;
    private GraphUpdaterManager graphUpdaterManager;
    private Graph graph;
    private String url;

    @Override
    protected void runPolling() {
        LOG.info("Polling vehicles from API");
        VehiclePositionsDiff diff = vehiclePositionsGetter.getVehiclePositionsDiff(graph, url);
        LOG.info("Got {} vehicles possible to place on a map", diff.getAppeared().size());
        VehicleSharingGraphWriterRunnable graphWriterRunnable =
                new VehicleSharingGraphWriterRunnable(rentableVehiclesLinker, diff.getAppeared());
        graphUpdaterManager.execute(graphWriterRunnable);
        LOG.info("Finished updating vehicles positions");
    }

    @Override
    protected void configurePolling(Graph graph, JsonNode config) throws IllegalStateException {
        this.pollingPeriodSeconds = 60;
        this.url = System.getProperty("sharedVehiclesApi");
        if (this.url == null) {
            throw new IllegalStateException("Please provide program parameter `--sharedVehiclesApi <URL>`");
        }
    }

    @Override
    public void configure(Graph graph, JsonNode config) throws Exception {
        configurePolling(graph, config);
    }

    @Override
    public void setGraphUpdaterManager(GraphUpdaterManager updaterManager) {
        this.graphUpdaterManager = updaterManager;
    }

    @Override
    public void setup(Graph graph) throws Exception {
        this.graph = graph;
        this.rentableVehiclesLinker = new RentableVehiclesLinker(
                TemporaryStreetSplitter.createNewDefaultInstance(graph, null, null)
        );
    }

    @Override
    public void teardown() {

    }
}
