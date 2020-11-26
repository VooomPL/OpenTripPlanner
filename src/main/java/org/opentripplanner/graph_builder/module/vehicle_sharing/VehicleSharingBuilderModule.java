package org.opentripplanner.graph_builder.module.vehicle_sharing;

import org.opentripplanner.graph_builder.services.GraphBuilderModule;
import org.opentripplanner.hasura_client.ParkingZonesGetter;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.edgetype.rentedgetype.DropoffVehicleEdge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.updater.vehicle_sharing.parking_zones.GeometryParkingZone;
import org.opentripplanner.updater.vehicle_sharing.parking_zones.ParkingZonesCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;

public class VehicleSharingBuilderModule implements GraphBuilderModule {

    private static final Logger LOG = LoggerFactory.getLogger(VehicleSharingBuilderModule.class);

    private final ParkingZonesGetter parkingZonesGetter = new ParkingZonesGetter();

    @Nullable
    private final String url;

    public VehicleSharingBuilderModule(String url) {
        this.url = url;
    }

    public static VehicleSharingBuilderModule withoutParkingZones() {
        return new VehicleSharingBuilderModule(null);
    }

    @Override
    public void buildGraph(Graph graph, HashMap<Class<?>, Object> extra) {
        if (url == null) {
            LOG.info("Creating vehicle dropoff edges without parking zones");
            createDropoffVehicleEdgesWithoutParkingZones(graph);
            LOG.info("Finished creating vehicle dropoff edges without parking zones");
        } else {
            LOG.info("Fetching parking zones from API");
            createParkingZonesCalculator(graph);
            LOG.info("Creating vehicle dropoff edges");
            createDropoffVehicleEdges(graph);
            LOG.info("Finished creating vehicle dropoff edges");
        }
    }

    private void createDropoffVehicleEdgesWithoutParkingZones(Graph graph) {
        graph.getVertices().stream()
                .filter(vertex -> vertex.getIncoming().stream().anyMatch(e -> e instanceof StreetEdge))
                .forEach(DropoffVehicleEdge::new);
    }

    private void createParkingZonesCalculator(Graph graph) {
        List<GeometryParkingZone> geometryParkingZones = parkingZonesGetter.postFromHasura(graph, url);
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
