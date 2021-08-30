package org.opentripplanner.graph_builder.module.vehicle_sharing;

import org.opentripplanner.graph_builder.linking.PermanentStreetSplitter;
import org.opentripplanner.graph_builder.services.GraphBuilderModule;
import org.opentripplanner.hasura_client.CityGovDropoffStationsGetter;
import org.opentripplanner.hasura_client.CityGovForbiddenZonesGetter;
import org.opentripplanner.hasura_client.MobilityHubDropoffStationsGetter;
import org.opentripplanner.hasura_client.MobilityHubForbiddenZonesGetter;
import org.opentripplanner.hasura_client.ParkingZonesGetter;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.edgetype.rentedgetype.CityGovDropoffStation;
import org.opentripplanner.routing.edgetype.rentedgetype.DropoffVehicleEdge;
import org.opentripplanner.routing.edgetype.rentedgetype.LowCostDropoffVehicleEdge;
import org.opentripplanner.routing.edgetype.rentedgetype.MobilityHubDropoffStation;
import org.opentripplanner.routing.edgetype.rentedgetype.MobilityHubParkingZoneInfo;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.vertextype.CityGovVehicleDropoffStationVertex;
import org.opentripplanner.routing.vertextype.MobilityHubVehicleDropoffStationVertex;
import org.opentripplanner.updater.vehicle_sharing.parking_zones.GeometriesDisallowedForVehicleType;
import org.opentripplanner.updater.vehicle_sharing.parking_zones.GeometryParkingZone;
import org.opentripplanner.updater.vehicle_sharing.parking_zones.GeometryParkingZonesMerger;
import org.opentripplanner.updater.vehicle_sharing.parking_zones.ParkingZonesCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;

import static java.util.Collections.emptyList;

public class VehicleSharingBuilderModule implements GraphBuilderModule {

    private static final Logger LOG = LoggerFactory.getLogger(VehicleSharingBuilderModule.class);

    private final ParkingZonesGetter parkingZonesGetter = new ParkingZonesGetter();

    private final CityGovForbiddenZonesGetter cityGovForbiddenZonesGetter = new CityGovForbiddenZonesGetter();

    private final CityGovDropoffStationsGetter cityGovDropoffStationsGetter = new CityGovDropoffStationsGetter();

    private final MobilityHubDropoffStationsGetter mobilityHubDropoffStationsGetter = new MobilityHubDropoffStationsGetter();

    private final MobilityHubForbiddenZonesGetter mobilityHubForbiddenZonesGetter = new MobilityHubForbiddenZonesGetter();

    private final GeometryParkingZonesMerger geometryParkingZonesMerger = new GeometryParkingZonesMerger();

    @Nullable
    private final String sharedVehiclesApiUrl;

    @Nullable
    private final String cityGovHasuraApiUrl;

    @Nullable
    private final String cityGovHasuraApiPassword;

    public VehicleSharingBuilderModule(String sharedVehiclesApiUrl, String cityGovHasuraApiUrl,
                                       String cityGovHasuraApiPassword) {
        this.sharedVehiclesApiUrl = sharedVehiclesApiUrl;
        this.cityGovHasuraApiUrl = cityGovHasuraApiUrl;
        this.cityGovHasuraApiPassword = cityGovHasuraApiPassword;
    }

    public static VehicleSharingBuilderModule justParkingZones(String sharedVehiclesApiUrl) {
        return new VehicleSharingBuilderModule(sharedVehiclesApiUrl, null, null);
    }

    public static VehicleSharingBuilderModule withoutParkingZones() {
        return new VehicleSharingBuilderModule(null, null, null);
    }

    @Override
    public void buildGraph(Graph graph, HashMap<Class<?>, Object> extra) {
        if (sharedVehiclesApiUrl == null) {
            LOG.info("Creating vehicle dropoff edges without parking zones");
            createDropoffVehicleEdgesWithoutParkingZones(graph);
            LOG.info("Finished creating vehicle dropoff edges without parking zones");
        } else {
            if (cityGovHasuraApiUrl == null || cityGovHasuraApiPassword == null) {
                LOG.info("Creating vehicle dropoff edges with just parking zones");
                createParkingZonesCalculatorWithNoCityGovForbiddenParkingZones(graph);
            } else {
                LOG.info("Creating vehicle dropoff edges with parking zones and city gov forbidden parking zones");
                createParkingZonesCalculator(graph);
                LOG.info("Creating city government vehicle dropoff stations");
                PermanentStreetSplitter splitter = PermanentStreetSplitter.createNewDefaultInstance(graph, null, false);
                createCityGovVehicleDropoffStations(graph, splitter);
                createMobilityHubVehicleDropoffStations(graph, splitter);
            }
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

    private void createParkingZonesCalculatorWithNoCityGovForbiddenParkingZones(Graph graph) {
        List<GeometryParkingZone> geometryParkingZones = parkingZonesGetter.postFromHasura(graph, sharedVehiclesApiUrl);
        graph.parkingZonesCalculator = new ParkingZonesCalculator(geometryParkingZones, emptyList());
    }

    private void createParkingZonesCalculator(Graph graph) {
        List<GeometryParkingZone> geometryParkingZones = parkingZonesGetter.postFromHasura(graph, sharedVehiclesApiUrl);
        List<GeometryParkingZone> mobilityHubForbiddenZones = mobilityHubForbiddenZonesGetter.postFromHasuraWithPassword(graph, cityGovHasuraApiUrl, cityGovHasuraApiPassword);
        List<GeometryParkingZone> mergedGeometries = geometryParkingZonesMerger.merge(geometryParkingZones, mobilityHubForbiddenZones);
        List<GeometriesDisallowedForVehicleType> cityGovForbiddenGeometryParkingZones = cityGovForbiddenZonesGetter
                .postFromHasuraWithPassword(graph, cityGovHasuraApiUrl, cityGovHasuraApiPassword);
        graph.parkingZonesCalculator = new ParkingZonesCalculator(mergedGeometries, cityGovForbiddenGeometryParkingZones);
    }

    private void createDropoffVehicleEdges(Graph graph) {
        graph.getVertices().stream()
                .filter(vertex -> vertex.getIncoming().stream().anyMatch(e -> e instanceof StreetEdge))
                .filter(vertex -> !(vertex instanceof CityGovVehicleDropoffStationVertex))
                .forEach(vertex -> createDropoffVehicleEdge(graph, vertex));
    }

    private void createDropoffVehicleEdge(Graph graph, Vertex vertex) {
        new DropoffVehicleEdge(vertex, graph.parkingZonesCalculator.getParkingZonesForLocation(vertex));
    }

    private void createCityGovVehicleDropoffStations(Graph graph, PermanentStreetSplitter splitter) {
        cityGovDropoffStationsGetter.postFromHasuraWithPassword(graph, cityGovHasuraApiUrl, cityGovHasuraApiPassword)
                .forEach(station -> createCityGovVehicleDropoffStation(graph, splitter, station));
    }

    private void createCityGovVehicleDropoffStation(Graph graph, PermanentStreetSplitter splitter,
                                                    CityGovDropoffStation station) {
        CityGovVehicleDropoffStationVertex vertex = new CityGovVehicleDropoffStationVertex(graph, station);
        if (splitter.link(vertex)) {
            new DropoffVehicleEdge(vertex, graph.parkingZonesCalculator.getParkingZonesForLocation(vertex,
                    station.getVehicleType()));
        } else {
            LOG.warn("Failed to create city government vehicle dropoff station at {},{}", station.getLongitude(),
                    station.getLatitude());
        }
    }

    private void createMobilityHubVehicleDropoffStations(Graph graph, PermanentStreetSplitter splitter) {
        mobilityHubDropoffStationsGetter.postFromHasuraWithPassword(graph, cityGovHasuraApiUrl, cityGovHasuraApiPassword)
                .forEach(station -> createMobilityHubVehicleDropoffStation(graph, splitter, station));
    }

    private void createMobilityHubVehicleDropoffStation(Graph graph, PermanentStreetSplitter splitter,
                                                        MobilityHubDropoffStation station) {
        MobilityHubVehicleDropoffStationVertex vertex = new MobilityHubVehicleDropoffStationVertex(graph, station);
        if (splitter.link(vertex)) {
            new LowCostDropoffVehicleEdge(vertex, new MobilityHubParkingZoneInfo(station.getParkingZones()));
        } else {
            LOG.warn("Failed to create mobility hub vehicle dropoff station at {},{}", station.getLongitude(),
                    station.getLatitude());
        }
    }

    @Override
    public void checkInputs() {
    }
}
