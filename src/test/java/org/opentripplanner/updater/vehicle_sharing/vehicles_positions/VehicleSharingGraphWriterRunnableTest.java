package org.opentripplanner.updater.vehicle_sharing.vehicles_positions;

import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.geom.CoordinateXY;
import org.opentripplanner.graph_builder.linking.TemporaryStreetSplitter;
import org.opentripplanner.routing.core.vehicle_sharing.*;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.vertextype.TemporaryRentVehicleVertex;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;

import static java.util.Collections.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class VehicleSharingGraphWriterRunnableTest {

    private static final CarDescription CAR_1 = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"));
    private static final CarDescription CAR_2 = new CarDescription("2", 1, 1, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"));

    private Graph graph;

    private TemporaryStreetSplitter temporaryStreetSplitter;

    private TemporaryRentVehicleVertex vertex;

    private TemporaryRentVehicleVertex vertex2;

    @Before
    public void setUp() {
        graph = new Graph();

        temporaryStreetSplitter = mock(TemporaryStreetSplitter.class);

        vertex = new TemporaryRentVehicleVertex("id", new CoordinateXY(1, 2), "name");
        vertex2 = new TemporaryRentVehicleVertex("id2", new CoordinateXY(2, 2), "name2");
    }

    @Test
    public void shouldAddAppearedRentableVehicles() {
        // given
        when(temporaryStreetSplitter.linkRentableVehicleToGraph(CAR_1)).thenReturn(Optional.of(vertex));
        VehicleSharingGraphWriterRunnable runnable = new VehicleSharingGraphWriterRunnable(temporaryStreetSplitter,
                singletonList(CAR_1), singleton(new Provider(2, "PANEK")));

        // when
        runnable.run(graph);

        // then
        assertEquals(1, graph.vehiclesTriedToLink.size());
        assertTrue(graph.vehiclesTriedToLink.containsKey(CAR_1));
        verify(temporaryStreetSplitter, times(1)).linkRentableVehicleToGraph(CAR_1);
        verifyNoMoreInteractions(temporaryStreetSplitter);
        assertEquals(1, graph.getLastProviderVehiclesUpdateTimestamps().size());
        assertTrue(graph.getLastProviderVehiclesUpdateTimestamps().containsKey(new Provider(2, "PANEK")));
    }

    @Test
    public void shouldAddSameVehicleFromDifferentSnapshot() {
        //when
        CarDescription car1FromSnapshot1 = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"));
        car1FromSnapshot1.setSnapshotLabel(new SharedVehiclesSnapshotLabel(LocalDateTime.of(2021, 1, 15, 10, 0)));
        when(temporaryStreetSplitter.linkRentableVehicleToGraph(car1FromSnapshot1)).thenReturn(Optional.of(vertex));

        // given
        graph.vehiclesTriedToLink.put(CAR_1, Optional.of(vertex));
        VehicleSharingGraphWriterRunnable runnable = new VehicleSharingGraphWriterRunnable(temporaryStreetSplitter,
                singletonList(car1FromSnapshot1), singleton(new Provider(2, "PANEK")), new SharedVehiclesSnapshotLabel(LocalDateTime.of(2021, 1, 15, 10, 0)));

        // when
        runnable.run(graph);

        // then
        assertEquals(2, graph.vehiclesTriedToLink.size());
        assertTrue(graph.vehiclesTriedToLink.containsKey(CAR_1));
        assertTrue(graph.vehiclesTriedToLink.containsKey(car1FromSnapshot1));
        verify(temporaryStreetSplitter, times(1)).linkRentableVehicleToGraph(car1FromSnapshot1);
        verifyNoMoreInteractions(temporaryStreetSplitter);
        //Grace period associated data is not supposed to be modified when adding historical vehicle locations from API
        assertEquals(0, graph.getLastProviderVehiclesUpdateTimestamps().size());
    }

    @Test
    public void shouldNotAddVehicleFromSnapshotIncompatibleWithGraphWriterRunnableLabel() {
        //when
        when(temporaryStreetSplitter.linkRentableVehicleToGraph(CAR_2)).thenReturn(Optional.of(vertex2));

        // given
        graph.vehiclesTriedToLink.put(CAR_1, Optional.of(vertex));
        VehicleSharingGraphWriterRunnable runnable = new VehicleSharingGraphWriterRunnable(temporaryStreetSplitter,
                singletonList(CAR_2), singleton(new Provider(2, "PANEK")), new SharedVehiclesSnapshotLabel(LocalDateTime.of(2021, 1, 15, 10, 0)));

        // when
        runnable.run(graph);

        // then
        assertEquals(1, graph.vehiclesTriedToLink.size());
        assertTrue(graph.vehiclesTriedToLink.containsKey(CAR_1));
        assertTrue(CAR_2.getSnapshotLabel().isEmpty());
        assertFalse(graph.vehiclesTriedToLink.containsKey(CAR_2));
        verifyZeroInteractions(temporaryStreetSplitter);
        assertEquals(0, graph.getLastProviderVehiclesUpdateTimestamps().size());
    }

    @Test
    public void shouldRemoveDisappearedRentableVehicles() {
        // given
        graph.vehiclesTriedToLink.put(CAR_1, Optional.of(vertex));
        graph.getLastProviderVehiclesUpdateTimestamps().put(CAR_1.getProvider(),
                LocalTime.now().minus(Graph.REMOVE_UNRESPONSIVE_PROVIDER_LIMIT_SECONDS + 1, ChronoUnit.SECONDS));
        VehicleSharingGraphWriterRunnable runnable = new VehicleSharingGraphWriterRunnable(temporaryStreetSplitter,
                emptyList(), emptySet());

        // when
        runnable.run(graph);

        // then
        assertTrue(graph.vehiclesTriedToLink.isEmpty());
        verifyZeroInteractions(temporaryStreetSplitter);
        assertTrue(graph.getLastProviderVehiclesUpdateTimestamps().isEmpty());
    }

    @Test
    public void shouldNotRemoveDisappearedRentableVehiclesDueToGracePeriod() {
        // given
        graph.vehiclesTriedToLink.put(CAR_1, Optional.of(vertex));
        graph.getLastProviderVehiclesUpdateTimestamps().put(CAR_1.getProvider(),
                LocalTime.now().minus(Graph.REMOVE_UNRESPONSIVE_PROVIDER_LIMIT_SECONDS, ChronoUnit.SECONDS));
        VehicleSharingGraphWriterRunnable runnable = new VehicleSharingGraphWriterRunnable(temporaryStreetSplitter,
                emptyList(), emptySet());

        // when
        runnable.run(graph);

        // then
        assertEquals(1, graph.vehiclesTriedToLink.size());
        assertTrue(graph.vehiclesTriedToLink.containsKey(CAR_1));
        verifyZeroInteractions(temporaryStreetSplitter);
        assertEquals(1, graph.getLastProviderVehiclesUpdateTimestamps().size());
        assertTrue(graph.getLastProviderVehiclesUpdateTimestamps().containsKey(new Provider(2, "PANEK")));
    }

    @Test
    public void shouldRemoveDisappearedRentableVehiclesAsProviderIsResponsive() {
        graph.vehiclesTriedToLink.put(CAR_1, Optional.of(vertex));
        when(temporaryStreetSplitter.linkRentableVehicleToGraph(CAR_2)).thenReturn(Optional.of(vertex2));
        //Grace period for provider is not exceeded
        graph.getLastProviderVehiclesUpdateTimestamps().put(CAR_1.getProvider(),
                LocalTime.now().minus(Graph.REMOVE_UNRESPONSIVE_PROVIDER_LIMIT_SECONDS, ChronoUnit.SECONDS));
        //Provider did send some new vehicle, but did not send information about the previously mapped one
        VehicleSharingGraphWriterRunnable runnable = new VehicleSharingGraphWriterRunnable(temporaryStreetSplitter,
                Collections.singletonList(CAR_2), singleton(new Provider(2, "PANEK")));

        // when
        runnable.run(graph);

        // then
        assertEquals(1, graph.vehiclesTriedToLink.size());
        assertFalse(graph.vehiclesTriedToLink.containsKey(CAR_1));
        assertTrue(graph.vehiclesTriedToLink.containsKey(CAR_2));
        verify(temporaryStreetSplitter, times(1)).linkRentableVehicleToGraph(CAR_2);
        verifyNoMoreInteractions(temporaryStreetSplitter);
        assertEquals(1, graph.getLastProviderVehiclesUpdateTimestamps().size());
        assertTrue(graph.getLastProviderVehiclesUpdateTimestamps().containsKey(new Provider(2, "PANEK")));
    }

    @Test
    public void shouldNotModifyProviderResponsivenessTimestampWhenRunningHistoricalGraphWriterRunnable() {
        LocalTime lastProviderVehiclesUpdateTimestamp = LocalTime.now().minus(Graph.REMOVE_UNRESPONSIVE_PROVIDER_LIMIT_SECONDS, ChronoUnit.SECONDS);
        graph.getLastProviderVehiclesUpdateTimestamps().put(CAR_1.getProvider(), lastProviderVehiclesUpdateTimestamp);

        VehicleSharingGraphWriterRunnable runnable = new VehicleSharingGraphWriterRunnable(temporaryStreetSplitter,
                emptyList(), null);

        // when
        runnable.run(graph);

        // then
        assertTrue(graph.getLastProviderVehiclesUpdateTimestamps().get(CAR_1.getProvider()).equals(lastProviderVehiclesUpdateTimestamp));
    }

    @Test
    public void shouldNotRemoveOneOfDisappearedRentableVehicleDueToIncompatibleSnapshotLabel() {
        //CAR_2 has a default empty snapshot label (-1)
        graph.vehiclesTriedToLink.put(CAR_2, Optional.of(vertex2));

        VehicleDescription carFromSnapshot1 = new CarDescription("2", 1, 1, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"));
        carFromSnapshot1.setSnapshotLabel(new SharedVehiclesSnapshotLabel(LocalDateTime.of(2021, 1, 15, 10, 0)));
        TemporaryRentVehicleVertex snapshotVertex = new TemporaryRentVehicleVertex("id_snapshot", new CoordinateXY(2, 2), "nam_snapshot");
        graph.vehiclesTriedToLink.put(carFromSnapshot1, Optional.of(snapshotVertex));

        //GraphWriter was created for the default empty snapshot label (no label specified in the constructor)
        VehicleSharingGraphWriterRunnable runnable = new VehicleSharingGraphWriterRunnable(temporaryStreetSplitter,
                emptyList(), null);

        // when
        runnable.run(graph);

        // then
        assertEquals(1, graph.vehiclesTriedToLink.size());
        /*
         * carFromSnapshot1 was not removed, because GraphWriterRunnable was created to affect current vehicle locations
         * (it was created with the default empty snapshot label)
         */
        assertTrue(graph.vehiclesTriedToLink.containsKey(carFromSnapshot1));
        verifyZeroInteractions(temporaryStreetSplitter);
        assertEquals(0, graph.getLastProviderVehiclesUpdateTimestamps().size());
    }

    @Test
    public void shouldPreserveExistingRentableVehicles() {
        // given
        graph.vehiclesTriedToLink.put(CAR_1, Optional.of(vertex));
        VehicleSharingGraphWriterRunnable runnable = new VehicleSharingGraphWriterRunnable(temporaryStreetSplitter,
                singletonList(CAR_1), singleton(new Provider(2, "PANEK")));

        // when
        runnable.run(graph);

        // then
        assertEquals(1, graph.vehiclesTriedToLink.size());
        assertTrue(graph.vehiclesTriedToLink.containsKey(CAR_1));
        verifyZeroInteractions(temporaryStreetSplitter);
        assertEquals(1, graph.getLastProviderVehiclesUpdateTimestamps().size());
        assertTrue(graph.getLastProviderVehiclesUpdateTimestamps().containsKey(new Provider(2, "PANEK")));
    }

    @Test
    public void shouldPreserveRentableVehicleFromBothSnapshots() {
        // given
        graph.vehiclesTriedToLink.put(CAR_1, Optional.of(vertex));

        SharedVehiclesSnapshotLabel snapshotLabel1 = new SharedVehiclesSnapshotLabel(LocalDateTime.of(2021, 1, 15, 10, 0));
        VehicleDescription carFromSnapshot1 = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"));
        carFromSnapshot1.setSnapshotLabel(snapshotLabel1);
        TemporaryRentVehicleVertex snapshotVertex = new TemporaryRentVehicleVertex("id_snapshot", new CoordinateXY(0, 0), "nam_snapshot");
        graph.vehiclesTriedToLink.put(carFromSnapshot1, Optional.of(snapshotVertex));

        VehicleSharingGraphWriterRunnable runnable = new VehicleSharingGraphWriterRunnable(temporaryStreetSplitter,
                singletonList(carFromSnapshot1), singleton(new Provider(2, "PANEK")), snapshotLabel1);

        // when
        runnable.run(graph);

        // then
        assertEquals(2, graph.vehiclesTriedToLink.size());
        assertTrue(graph.vehiclesTriedToLink.containsKey(CAR_1));
        assertTrue(graph.vehiclesTriedToLink.containsKey(carFromSnapshot1));
        verifyZeroInteractions(temporaryStreetSplitter);
        assertEquals(0, graph.getLastProviderVehiclesUpdateTimestamps().size());
    }

    @Test
    public void shouldAddRentableVehicleFromBothSnapshotGraphWriters() {
        // given
        when(temporaryStreetSplitter.linkRentableVehicleToGraph(CAR_1)).thenReturn(Optional.of(vertex));

        SharedVehiclesSnapshotLabel snapshotLabel1 = new SharedVehiclesSnapshotLabel(LocalDateTime.of(2021, 1, 15, 10, 0));
        VehicleDescription carFromSnapshot1 = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"));
        carFromSnapshot1.setSnapshotLabel(snapshotLabel1);
        TemporaryRentVehicleVertex snapshotVertex = new TemporaryRentVehicleVertex("id_snapshot", new CoordinateXY(0, 0), "nam_snapshot");
        when(temporaryStreetSplitter.linkRentableVehicleToGraph(carFromSnapshot1)).thenReturn(Optional.of(snapshotVertex));

        VehicleSharingGraphWriterRunnable runnableCurrent = new VehicleSharingGraphWriterRunnable(temporaryStreetSplitter,
                singletonList(CAR_1), singleton(new Provider(2, "PANEK")));
        VehicleSharingGraphWriterRunnable runnableSnapshot = new VehicleSharingGraphWriterRunnable(temporaryStreetSplitter,
                singletonList(carFromSnapshot1), singleton(new Provider(2, "PANEK")), snapshotLabel1);

        // when
        runnableCurrent.run(graph);
        runnableSnapshot.run(graph);

        // then
        assertEquals(2, graph.vehiclesTriedToLink.size());
        assertTrue(graph.vehiclesTriedToLink.containsKey(CAR_1));
        assertTrue(graph.vehiclesTriedToLink.containsKey(carFromSnapshot1));
        assertTrue(graph.vehiclesTriedToLink.get(carFromSnapshot1).isPresent());
    }

    @Test
    public void shouldOnlyRemoveVehicleFromSnapshot1GraphWriters() {
        // given
        when(temporaryStreetSplitter.linkRentableVehicleToGraph(CAR_1)).thenReturn(Optional.of(vertex));

        SharedVehiclesSnapshotLabel snapshotLabel1 = new SharedVehiclesSnapshotLabel(LocalDateTime.of(2021, 1, 15, 10, 0));
        VehicleDescription carFromSnapshot1 = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"));
        carFromSnapshot1.setSnapshotLabel(snapshotLabel1);
        TemporaryRentVehicleVertex snapshotVertex = new TemporaryRentVehicleVertex("id_snapshot", new CoordinateXY(0, 0), "nam_snapshot");
        graph.vehiclesTriedToLink.put(carFromSnapshot1, Optional.of(snapshotVertex));

        VehicleSharingGraphWriterRunnable runnableCurrent = new VehicleSharingGraphWriterRunnable(temporaryStreetSplitter,
                singletonList(CAR_1), singleton(new Provider(2, "PANEK")));
        VehicleSharingGraphWriterRunnable runnableSnapshot = new VehicleSharingGraphWriterRunnable(temporaryStreetSplitter,
                emptyList(), null, snapshotLabel1);

        // when
        runnableCurrent.run(graph);
        runnableSnapshot.run(graph);

        // then
        assertEquals(1, graph.vehiclesTriedToLink.size());
        assertTrue(graph.vehiclesTriedToLink.containsKey(CAR_1));
        assertFalse(graph.vehiclesTriedToLink.containsKey(carFromSnapshot1));
    }

    @Test
    public void shouldOnlyAddProperlyLinkedRentableVehicleVertexFromSnapshotGraphWriter() {
        // given
        when(temporaryStreetSplitter.linkRentableVehicleToGraph(CAR_1)).thenReturn(Optional.of(vertex));

        SharedVehiclesSnapshotLabel snapshotLabel1 = new SharedVehiclesSnapshotLabel(LocalDateTime.of(2021, 1, 15, 10, 0));
        VehicleDescription carFromSnapshot1 = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"));
        carFromSnapshot1.setSnapshotLabel(snapshotLabel1);
        when(temporaryStreetSplitter.linkRentableVehicleToGraph(carFromSnapshot1)).thenReturn(Optional.empty());

        VehicleSharingGraphWriterRunnable runnableCurrent = new VehicleSharingGraphWriterRunnable(temporaryStreetSplitter,
                singletonList(CAR_1), singleton(new Provider(2, "PANEK")));
        VehicleSharingGraphWriterRunnable runnableSnapshot = new VehicleSharingGraphWriterRunnable(temporaryStreetSplitter,
                singletonList(carFromSnapshot1), singleton(new Provider(2, "PANEK")), snapshotLabel1);

        // when
        runnableCurrent.run(graph);
        runnableSnapshot.run(graph);

        // then
        assertEquals(2, graph.vehiclesTriedToLink.size());
        assertTrue(graph.vehiclesTriedToLink.containsKey(CAR_1));
        assertTrue(graph.vehiclesTriedToLink.containsKey(carFromSnapshot1));
        assertFalse(graph.vehiclesTriedToLink.get(carFromSnapshot1).isPresent());
    }

}
