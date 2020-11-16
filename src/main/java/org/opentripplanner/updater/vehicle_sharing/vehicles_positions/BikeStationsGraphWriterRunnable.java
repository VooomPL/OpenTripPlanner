package org.opentripplanner.updater.vehicle_sharing.vehicles_positions;

import org.opentripplanner.graph_builder.linking.TemporaryStreetSplitter;
import org.opentripplanner.routing.bike_rental.BikeRentalStation;
import org.opentripplanner.routing.edgetype.rentedgetype.DropBikeEdge;
import org.opentripplanner.routing.edgetype.rentedgetype.RentBikeEdge;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.vertextype.TemporaryRentVehicleVertex;
import org.opentripplanner.routing.vertextype.TemporaryVertex;
import org.opentripplanner.updater.GraphWriterRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This edge allows us to rent bike from station (or leave current vehicle and rent bike).
 * This edge is a loop on {@link TemporaryRentVehicleVertex} which, when traversed, changes our current traverse mode,
 * but leaves us in the same location. Edge is dependent on {@link BikeRentalStation}. Renting a bike is impossible
 * if station is empty.
 */
public class BikeStationsGraphWriterRunnable implements GraphWriterRunnable {

    private static final Logger LOG = LoggerFactory.getLogger(BikeStationsGraphWriterRunnable.class);
    private final TemporaryStreetSplitter temporaryStreetSplitter;
    private final List<BikeRentalStation> bikeRentalStationsFetchedFromApi;

    public BikeStationsGraphWriterRunnable(TemporaryStreetSplitter temporaryStreetSplitter, List<BikeRentalStation> bikeRentalStations) {
        this.temporaryStreetSplitter = temporaryStreetSplitter;
        this.bikeRentalStationsFetchedFromApi = bikeRentalStations;
    }

    private boolean addBikeStationToGraph(BikeRentalStation station, Graph graph) {
        Optional<TemporaryRentVehicleVertex> vertex = temporaryStreetSplitter.linkBikeRentalStationToGraph(station);
        if (!vertex.isPresent()) {
            return false;
        }
        new DropBikeEdge(vertex.get(), station);
        RentBikeEdge edge = vertex.get().getOutgoing().stream()
                .filter(RentBikeEdge.class::isInstance)
                .map(RentBikeEdge.class::cast)
                .findFirst().get();
        graph.bikeRentalStationsInGraph.put(station, edge);
        return true;
    }

    private void updateBikeStationInfo(BikeRentalStation station, RentBikeEdge rentBikeEdge) {
        rentBikeEdge.getBikeRentalStation().bikesAvailable = station.bikesAvailable;
        rentBikeEdge.getBikeRentalStation().spacesAvailable = station.spacesAvailable;
    }


    @Override
    public void run(Graph graph) {
        int updatedStationsCount = 0;
        int removedStationsCount;
        int placedStationsCount = 0;
        int failedToPlaceCount = 0;
        Map<BikeRentalStation, RentBikeEdge> oldStations = graph.bikeRentalStationsInGraph;
        graph.bikeRentalStationsInGraph = new HashMap<>();

        for (BikeRentalStation station : bikeRentalStationsFetchedFromApi) {
            RentBikeEdge oldStationEdge = oldStations.getOrDefault(station, null);
            if (oldStationEdge != null) {
                updatedStationsCount++;
                updateBikeStationInfo(station, oldStationEdge);
                oldStations.remove(station);
                graph.bikeRentalStationsInGraph.put(station, oldStationEdge);
            } else {
                if (addBikeStationToGraph(station, graph))
                    placedStationsCount++;
                else
                    failedToPlaceCount++;
            }
        }
        List<Vertex> dissappearedStations = oldStations.values().stream()
                .map(Edge::getFromVertex)
                .collect(Collectors.toList());
        removedStationsCount = dissappearedStations.size();

        TemporaryVertex.disposeAll(dissappearedStations);

        LOG.info("Placed {} bike stations on a map", placedStationsCount);
        LOG.info("Failed to place {} bike stations on a map", failedToPlaceCount);
        LOG.info("Updated {} bike stations on a map", updatedStationsCount);
        LOG.info("Removed {} bike stations from map", removedStationsCount);
    }
}

