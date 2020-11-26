package org.opentripplanner.graph_builder.module.vehicle_sharing;

import org.opentripplanner.graph_builder.linking.PermanentStreetSplitter;
import org.opentripplanner.graph_builder.services.GraphBuilderModule;
import org.opentripplanner.hasura_client.CityGovDropoffStationsGetter;
import org.opentripplanner.hasura_client.CityGovForbiddenZonesGetter;
import org.opentripplanner.hasura_client.ParkingZonesGetter;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.edgetype.rentedgetype.CityGovDropoffStation;
import org.opentripplanner.routing.edgetype.rentedgetype.DropoffVehicleEdge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.vertextype.CityGovVehicleDropoffStationVertex;
import org.opentripplanner.updater.vehicle_sharing.parking_zones.GeometriesDisallowedForVehicleType;
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

    private final CityGovForbiddenZonesGetter cityGovForbiddenZonesGetter = new CityGovForbiddenZonesGetter();

    private final CityGovDropoffStationsGetter cityGovDropoffStationsGetter = new CityGovDropoffStationsGetter();

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
            LOG.info("Creating city government vehicle dropoff stations");
            createCityGovVehicleDropoffStations(graph);
            LOG.info("Finished creating vehicle dropoff edges and stations");
        }
    }

    private void createDropoffVehicleEdgesWithoutParkingZones(Graph graph) {
        graph.getVertices().stream()
                .filter(vertex -> vertex.getIncoming().stream().anyMatch(e -> e instanceof StreetEdge))
                .forEach(DropoffVehicleEdge::new);
    }

    private void createParkingZonesCalculator(Graph graph) {
        List<GeometryParkingZone> geometryParkingZones = parkingZonesGetter.postFromHasura(graph, url);
        List<GeometriesDisallowedForVehicleType> cityGovForbiddenGeometryParkingZones =
                cityGovForbiddenZonesGetter.postFromHasura(graph, url);
        graph.parkingZonesCalculator = new ParkingZonesCalculator(geometryParkingZones,
                cityGovForbiddenGeometryParkingZones);
    }

    private void createDropoffVehicleEdges(Graph graph) {
        graph.getVertices().stream()
                .filter(vertex -> vertex.getIncoming().stream().anyMatch(e -> e instanceof StreetEdge))
                .forEach(vertex -> createDropoffVehicleEdge(graph, vertex));
    }

    private void createDropoffVehicleEdge(Graph graph, Vertex vertex) {
        new DropoffVehicleEdge(vertex, graph.parkingZonesCalculator.getParkingZonesForLocation(vertex));
    }

    private void createCityGovVehicleDropoffStations(Graph graph) {
        PermanentStreetSplitter splitter = PermanentStreetSplitter.createNewDefaultInstance(graph, null, false);
        cityGovDropoffStationsGetter.getFromHasura(graph, url)
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

    @Override
    public void checkInputs() {
    }
}
