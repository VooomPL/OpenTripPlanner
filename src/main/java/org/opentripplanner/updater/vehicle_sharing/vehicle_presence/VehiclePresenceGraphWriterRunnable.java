package org.opentripplanner.updater.vehicle_sharing.vehicle_presence;

import org.opentripplanner.prediction_client.VehiclePresence;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleType;
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
        if(vehiclePresenceHeatmapsFromApi.getVehicleType().equalsIgnoreCase(VehicleType.CAR.name())) {
            LOG.info("Updating vehicle presence prediction heatmaps from API");
            graph.carPresencePredictor = new CarPresencePredictor(vehiclePresenceHeatmapsFromApi);
        }
    }

    public VehiclePresenceGraphWriterRunnable(VehiclePresence vehiclePresenceHeatmap) {
        this.vehiclePresenceHeatmapsFromApi = vehiclePresenceHeatmap;
    }
}
