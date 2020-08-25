package org.opentripplanner.graph_builder.module.vehicle_sharing;

import org.opentripplanner.graph_builder.services.GraphBuilderModule;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.edgetype.rentedgetype.DropoffVehicleEdge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.updater.vehicle_sharing.parking_zones.GeometryParkingZone;
import org.opentripplanner.updater.vehicle_sharing.parking_zones.ParkingZonesCalculator;
import org.opentripplanner.updater.vehicle_sharing.parking_zones.ParkingZonesGetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;

// TODO AdamWiktor VMP-37 add tests
public class VehicleSharingBuilderModule implements GraphBuilderModule {

    private static final Logger LOG = LoggerFactory.getLogger(VehicleSharingBuilderModule.class);

    private final ParkingZonesGetter parkingZonesGetter = new ParkingZonesGetter();

    private final String url;

    public VehicleSharingBuilderModule(String url) {
        this.url = url;
    }

    @Override
    public void buildGraph(Graph graph, HashMap<Class<?>, Object> extra) {
        LOG.info("Fetching parking zones from API");
        createParkingZonesCalculator(graph);
        LOG.info("Calculating parking zones for each vertex");
        createDropoffVehicleEdges(graph);
        LOG.info("Finished calculating parking zones for rentable vehicles");
    }

    private void createParkingZonesCalculator(Graph graph) {
        List<GeometryParkingZone> geometryParkingZones = parkingZonesGetter.getParkingZones(url, graph);
        graph.parkingZonesCalculator = new ParkingZonesCalculator(geometryParkingZones);
    }

    private void createDropoffVehicleEdges(Graph graph) {
        graph.getVertices().stream()
                .filter(vertex -> vertex.getIncoming().stream().anyMatch(e -> e instanceof StreetEdge))
                .forEach(vertex -> createDropoffVehicleEdge(graph, vertex));
    }

    private void createDropoffVehicleEdge(Graph graph, Vertex vertex) {
        new DropoffVehicleEdge(vertex, graph.parkingZonesCalculator.getParkingZonesForLocation(vertex));
    }

    @Override
    public void checkInputs() {

    }
}
