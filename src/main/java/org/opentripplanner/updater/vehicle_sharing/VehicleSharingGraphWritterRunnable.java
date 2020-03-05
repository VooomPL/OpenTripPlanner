package org.opentripplanner.updater.vehicle_sharing;

import org.opentripplanner.routing.edgetype.rentedgetype.RentVehicleAnywhereEdge;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.updater.GraphWriterRunnable;

import java.util.List;

class VehicleSharingGraphWritterRunnable implements GraphWriterRunnable {
    List<Edge> appeared;
    List<Edge> disappeared;

    public VehicleSharingGraphWritterRunnable(List<Edge> appeared, List<Edge> disappeared) {
        this.appeared = appeared;
        this.disappeared = disappeared;
    }

    @Override
    public void run(Graph graph) {
        for (Edge edge : appeared) {
            ((RentVehicleAnywhereEdge) edge).isAvaiable ++;
        }

        for (Edge edge : disappeared) {
            ((RentVehicleAnywhereEdge) edge).isAvaiable --;
        }

    }
}
