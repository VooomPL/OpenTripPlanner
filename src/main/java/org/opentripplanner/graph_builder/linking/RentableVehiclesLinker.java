package org.opentripplanner.graph_builder.linking;

import org.opentripplanner.routing.core.vehicle_sharing.VehicleDescription;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.vertextype.TemporaryVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class RentableVehiclesLinker {

    private static final Logger LOG = LoggerFactory.getLogger(RentableVehiclesLinker.class);

    private final TemporaryStreetSplitter temporaryStreetSplitter;

    private final Map<VehicleDescription, Vertex> vehiclesPreviouslyLinked = new HashMap<>();

    public RentableVehiclesLinker(TemporaryStreetSplitter temporaryStreetSplitter) {
        this.temporaryStreetSplitter = temporaryStreetSplitter;
    }

    public void removeAllLinkedRentableVehicles(Collection<VehicleDescription> vehiclesFetchedFromApi) {
        Map<VehicleDescription, Vertex> vehiclesToRemove = vehiclesPreviouslyLinked.entrySet()
                .stream()
                .filter(entry -> !vehiclesFetchedFromApi.contains(entry.getKey()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
        List<Vertex> vehiclesProperlyLinked = vehiclesToRemove.values()
                .stream()
                .filter(Objects::nonNull)
                .collect(toList());
        TemporaryVertex.disposeAll(vehiclesProperlyLinked);
        vehiclesToRemove.forEach(vehiclesPreviouslyLinked::remove);
        LOG.info("Removed {} vehicles from graph", vehiclesProperlyLinked.size());
    }

    public void linkRentableVehiclesToGraph(Collection<VehicleDescription> vehiclesFetchedFromApi) {
        int currentlyLinked = vehiclesPreviouslyLinked.size();
        vehiclesFetchedFromApi.stream()
                .filter(v -> !vehiclesPreviouslyLinked.containsKey(v))
                .forEach(v -> vehiclesPreviouslyLinked.put(v, temporaryStreetSplitter.linkRentableVehicleToGraph(v)));
        LOG.info("Linked {} new vehicles to graph", vehiclesPreviouslyLinked.size() - currentlyLinked);
        LOG.info("Currently there are {} vehicles linked to graph", vehiclesPreviouslyLinked.size());
    }
}
