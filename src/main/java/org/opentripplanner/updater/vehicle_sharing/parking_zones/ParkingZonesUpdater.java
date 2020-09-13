package org.opentripplanner.updater.vehicle_sharing.parking_zones;

import com.fasterxml.jackson.databind.JsonNode;
import org.opentripplanner.hasura_client.ParkingZonesGetter;
import org.opentripplanner.routing.edgetype.rentedgetype.DropoffVehicleEdge;
import org.opentripplanner.routing.edgetype.rentedgetype.ParkingZoneInfo;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.updater.GraphUpdaterManager;
import org.opentripplanner.updater.PollingGraphUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public class ParkingZonesUpdater extends PollingGraphUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(ParkingZonesUpdater.class);

    protected ParkingZonesGetter parkingZonesGetter = new ParkingZonesGetter();
    private GraphUpdaterManager graphUpdaterManager;
    private Graph graph;
    private String url;

    private DropoffVehicleEdge getRentVehicleAnywhereEdge(Vertex v) {
        return v.getOutgoing().stream()
                .filter(DropoffVehicleEdge.class::isInstance)
                .map(DropoffVehicleEdge.class::cast)
                .findAny()
                .orElse(null);
    }

    private Map<DropoffVehicleEdge, ParkingZoneInfo> getNewParkingZones(ParkingZonesCalculator calculator) {
        return graph.getVertices().stream()
                .map(this::getRentVehicleAnywhereEdge)
                .filter(Objects::nonNull)
                .collect(toMap(identity(), calculator::getParkingZonesForEdge));
    }

    @Override
    protected void runPolling() {
        LOG.info("Polling parking zones from API");
        List<GeometryParkingZone> geometryParkingZones = parkingZonesGetter.getFromHasura(graph, url);
        ParkingZonesCalculator calculator = new ParkingZonesCalculator(geometryParkingZones);
        LOG.info("Calculating parking zones for each vertex");
        Map<DropoffVehicleEdge, ParkingZoneInfo> parkingZonesPerVertex = getNewParkingZones(calculator);
        ParkingZonesGraphWriterRunnable graphWriterRunnable =
                new ParkingZonesGraphWriterRunnable(calculator, parkingZonesPerVertex);
        LOG.info("Executing parking zones update");
        graphUpdaterManager.execute(graphWriterRunnable);
        LOG.info("Finished updating parking zones");
    }

    @Override
    protected void configurePolling(Graph graph, JsonNode config) throws IllegalStateException {
        this.pollingPeriodSeconds = 60 * 60 * 24;
        this.url = System.getProperty("sharedVehiclesApi");
        if (this.url == null) {
            throw new IllegalStateException("Please provide program parameter `--sharedVehiclesApi <URL>`");
        }
    }

    @Override
    public void configure(Graph graph, JsonNode config) throws Exception {
        configurePolling(graph, config);
        type = "Parking Zones";
    }

    @Override
    public void setGraphUpdaterManager(GraphUpdaterManager updaterManager) {
        this.graphUpdaterManager = updaterManager;
    }

    @Override
    public void setup(Graph graph) throws Exception {
        this.graph = graph;
    }

    @Override
    public void teardown() {

    }
}