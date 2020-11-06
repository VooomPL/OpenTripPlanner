package org.opentripplanner.updater.vehicle_sharing.vehicle_presence;

import org.opentripplanner.prediction_client.VehiclePresence;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.updater.GraphWriterRunnable;
import org.opentripplanner.updater.vehicle_sharing.vehicles_positions.BikeStationsGraphWriterRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VehiclePresenceGraphWriterRunnable implements GraphWriterRunnable {

    private static final Logger LOG = LoggerFactory.getLogger(BikeStationsGraphWriterRunnable.class);

    private final VehiclePresence vehiclePresenceHeatmapsFromApi;

    @Override
    public void run(Graph graph) {
        LOG.info("Updating vehicle presence prediction heatmaps from API");
        if (graph.carPresencePredictor != null) {
            graph.carPresencePredictor.updateVehiclePresenceHeatmap(vehiclePresenceHeatmapsFromApi);
        } else {
            LOG.warn("VehiclePresencePredictor is null when pooling from prediction API is running. Something went wrong.");
        }
    }

    public VehiclePresenceGraphWriterRunnable(VehiclePresence vehiclePresenceHeatmap) {
        this.vehiclePresenceHeatmapsFromApi = vehiclePresenceHeatmap;
    }
}
