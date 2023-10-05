package org.opentripplanner.updater.vehicle_sharing.vehicles_positions;

import org.opentripplanner.Spt;
import org.opentripplanner.graph_builder.linking.TemporaryStreetSplitter;
import org.opentripplanner.routing.core.vehicle_sharing.Provider;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleDescription;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleType;
import org.opentripplanner.routing.edgetype.rentedgetype.ShortcutEdge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.vertextype.TemporaryRentVehicleVertex;
import org.opentripplanner.routing.vertextype.TemporaryVertex;
import org.opentripplanner.updater.GraphWriterRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.filtering;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

class VehicleSharingGraphWriterRunnable implements GraphWriterRunnable {

    private static final Logger LOG = LoggerFactory.getLogger(VehicleSharingGraphWriterRunnable.class);

    private final TemporaryStreetSplitter temporaryStreetSplitter;

    private final List<VehicleDescription> vehiclesFetchedFromApi;

    private final Set<Provider> responsiveProvidersFetchedFromApi;

    private final boolean removalGracePeriodDisabled;

    private final LocalTime updateTimestamp;

    private final SharedVehiclesSnapshotLabel snapshotLabel;

    private int counter;

    VehicleSharingGraphWriterRunnable(TemporaryStreetSplitter temporaryStreetSplitter,
                                      List<VehicleDescription> vehiclesFetchedFromApi,
                                      Set<Provider> responsiveProvidersFetchedFromApi) {
        this(temporaryStreetSplitter, vehiclesFetchedFromApi, responsiveProvidersFetchedFromApi, new SharedVehiclesSnapshotLabel());
    }

    VehicleSharingGraphWriterRunnable(TemporaryStreetSplitter temporaryStreetSplitter,
                                      List<VehicleDescription> vehiclesFetchedFromApi,
                                      Set<Provider> responsiveProvidersFetchedFromApi,
                                      SharedVehiclesSnapshotLabel sharedVehiclesSnapshotLabel) {
        this.temporaryStreetSplitter = temporaryStreetSplitter;
        this.vehiclesFetchedFromApi = vehiclesFetchedFromApi;
        this.snapshotLabel = sharedVehiclesSnapshotLabel;
        this.updateTimestamp = LocalTime.now();
        if (Objects.nonNull(responsiveProvidersFetchedFromApi)) {
            //We don't want to enable grace period for historical data even if the list of responsive providers was set
            this.removalGracePeriodDisabled = !sharedVehiclesSnapshotLabel.isEmpty();
            this.responsiveProvidersFetchedFromApi = responsiveProvidersFetchedFromApi;
        } else {
            this.removalGracePeriodDisabled = true;
            this.responsiveProvidersFetchedFromApi = Collections.emptySet();
        }
    }

    @Override
    public void run(Graph graph) {
        if (!removalGracePeriodDisabled) {
            //We don't want to modify last update timestamp based on historical data from snapshots
            for (Provider responsiveProvider : responsiveProvidersFetchedFromApi) {
                graph.getLastProviderVehiclesUpdateTimestamps().put(responsiveProvider, updateTimestamp);
            }
        }
        removeDisappearedRentableVehicles(graph);
        addAppearedRentableVehicles(graph);
        //We don't want to modify last update timestamp based on historical data from snapshots
        if (!removalGracePeriodDisabled) {
            graph.getLastProviderVehiclesUpdateTimestamps().entrySet().removeIf(entry -> graph.isUnresponsiveGracePeriodExceeded(entry.getKey(), updateTimestamp));
        }
        graph.routerHealth.setVehiclePosition(true);
    }

    private void removeDisappearedRentableVehicles(Graph graph) {
        Map<VehicleDescription, Optional<TemporaryRentVehicleVertex>> disappearedVehicles = getDisappearedVehicles(graph);
        List<Vertex> properlyLinkedVertices = getProperlyLinkedVertices(disappearedVehicles.values());
        TemporaryVertex.disposeAll(properlyLinkedVertices);
        disappearedVehicles.forEach(graph.vehiclesTriedToLink::remove);
        LOG.info("Removed {} rentable vehicles from snapshot {} from graph", disappearedVehicles.size(), this.snapshotLabel);
        LOG.debug("Removed {} properly linked rentable vehicles from snapshot {} from graph", properlyLinkedVertices.size(), this.snapshotLabel);
    }

    private Map<VehicleDescription, Optional<TemporaryRentVehicleVertex>> getDisappearedVehicles(Graph graph) {
        return graph.vehiclesTriedToLink.entrySet().stream()
                .filter(entry ->
                        entry.getKey().getSnapshotLabel().equals(this.snapshotLabel) &&
                                !vehiclesFetchedFromApi.contains(entry.getKey()) &&
                                (removalGracePeriodDisabled ||
                                        responsiveProvidersFetchedFromApi.contains(entry.getKey().getProvider()) ||
                                        graph.isUnresponsiveGracePeriodExceeded(entry.getKey().getProvider(), updateTimestamp))

                )
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private List<Vertex> getProperlyLinkedVertices(Collection<Optional<TemporaryRentVehicleVertex>> disappearedVehicles) {
        return disappearedVehicles.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    private void addAppearedRentableVehicles(Graph graph) {
        getAppearedVehicles(graph).forEach(v -> graph.vehiclesTriedToLink.put(v, temporaryStreetSplitter.linkRentableVehicleToGraph(v)));

        Map<Boolean, Long> vehiclesForSnapshot =
                graph.vehiclesTriedToLink.entrySet().stream().collect(
                        filtering(e -> e.getKey().getSnapshotLabel().equals(this.snapshotLabel),
                                groupingBy(e -> e.getValue().isPresent(), counting())));
        long properlyLinkedVehicles = Optional.ofNullable(vehiclesForSnapshot.get(true)).orElse(0L);
        LOG.info("Currently there are {} properly linked rentable vehicles from snapshot {} in graph",
                properlyLinkedVehicles, this.snapshotLabel);
        if (!this.snapshotLabel.isEmpty()) {
            Integer properlyLinkedVehiclesAsInt;
            try {
                properlyLinkedVehiclesAsInt = Math.toIntExact(properlyLinkedVehicles);
            } catch (ArithmeticException e) {
                LOG.warn("The number of vehicles in the {} snapshot does not fit into Integer", snapshotLabel);
                properlyLinkedVehiclesAsInt = Integer.MAX_VALUE;
            }
            graph.getSupportedSnapshotLabels().replace(this.snapshotLabel, properlyLinkedVehiclesAsInt);
        }

        counter = 0;
        Map<VehicleDescription, Optional<TemporaryRentVehicleVertex>> collect = graph.vehiclesTriedToLink.entrySet().stream()
                .filter(e -> e.getKey().getVehicleType() == VehicleType.KICKSCOOTER)
                .filter(e -> e.getKey().getProvider().getProviderId() == 0 || e.getKey().getProvider().getProviderId() == 1)
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        collect.forEach((vehicle1, vertex1) -> {
            if (!graph.vehiclesSpt.containsKey(vehicle1.getProviderVehicleId())) {
                LOG.info("skipping vehicle1 {}", vehicle1.getProviderVehicleId());
                return;
            }
            Map<String, Spt.SptElement> data = graph.vehiclesSpt.get(vehicle1.getProviderVehicleId());
            collect.forEach((vehicle2, vertex2) -> {
                if (!vehicle1.equals(vehicle2)) {
                    addShortcutEdge(vehicle1, vehicle2, vertex1.get(), vertex2.get(), data);
                }
            });
        });

        LOG.info("Added {} shortcut edges", counter);

        LOG.info("There are {} rentable vehicles from snapshot {} which we failed to link to graph",
                Optional.ofNullable(vehiclesForSnapshot.get(false)).orElse(0L),
                this.snapshotLabel);
    }

    private List<VehicleDescription> getAppearedVehicles(Graph graph) {
        return vehiclesFetchedFromApi.stream()
                .filter(v -> v.getSnapshotLabel().equals(this.snapshotLabel) && !graph.vehiclesTriedToLink.containsKey(v))
                .collect(toList());
    }

    private void addShortcutEdge(VehicleDescription vehicle1, VehicleDescription vehicle2, TemporaryRentVehicleVertex vertex1, TemporaryRentVehicleVertex vertex2, Map<String, Spt.SptElement> data) {
        if (!data.containsKey(vehicle2.getProviderVehicleId())) {
            return;
        }
        Spt.SptElement elem = data.get(vehicle2.getProviderVehicleId());
        counter++;
        new ShortcutEdge(vertex1, vertex2, vehicle1, vehicle2, elem.getDistance(), (int) elem.getDuration(), elem.getWeight());
    }
}
