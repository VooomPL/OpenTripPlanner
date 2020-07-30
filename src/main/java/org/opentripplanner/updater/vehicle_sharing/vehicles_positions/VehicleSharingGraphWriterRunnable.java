package org.opentripplanner.updater.vehicle_sharing.vehicles_positions;

import org.opentripplanner.graph_builder.linking.RentableVehiclesLinker;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleDescription;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.updater.GraphWriterRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

class VehicleSharingGraphWriterRunnable implements GraphWriterRunnable {

    private static final Logger LOG = LoggerFactory.getLogger(VehicleSharingGraphWriterRunnable.class);

    private final RentableVehiclesLinker rentableVehiclesLinker;

    private final List<VehicleDescription> vehiclesFetchedFromApi;

    VehicleSharingGraphWriterRunnable(RentableVehiclesLinker rentableVehiclesLinker,
                                      List<VehicleDescription> vehiclesFetchedFromApi) {
        this.rentableVehiclesLinker = rentableVehiclesLinker;
        this.vehiclesFetchedFromApi = vehiclesFetchedFromApi;
    }

    @Override
    public void run(Graph graph) {
        LOG.info("Start removing old vehicles");
        rentableVehiclesLinker.removeAllLinkedRentableVehicles(vehiclesFetchedFromApi);
        LOG.info("Start linking new vehicles");
        rentableVehiclesLinker.linkRentableVehiclesToGraph(vehiclesFetchedFromApi);
        LOG.info("End linking new vehicles");
    }
}
