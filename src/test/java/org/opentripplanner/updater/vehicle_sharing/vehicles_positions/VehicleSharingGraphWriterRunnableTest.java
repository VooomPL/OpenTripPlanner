package org.opentripplanner.updater.vehicle_sharing.vehicles_positions;

import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.geom.CoordinateXY;
import org.opentripplanner.graph_builder.linking.TemporaryStreetSplitter;
import org.opentripplanner.routing.core.vehicle_sharing.CarDescription;
import org.opentripplanner.routing.core.vehicle_sharing.FuelType;
import org.opentripplanner.routing.core.vehicle_sharing.Gearbox;
import org.opentripplanner.routing.core.vehicle_sharing.Provider;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.vertextype.TemporaryRentVehicleVertex;

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
    public void shouldRemoveDisappearedRentableVehicles() {
        // given
        graph.vehiclesTriedToLink.put(CAR_1, Optional.of(vertex));
        graph.getLastProviderVehiclesUpdateTimestamps().put(CAR_1.getProvider(),
                LocalTime.now().minus(Graph.REMOVE_UNRESPONSIVE_PROVIDER_LIMIT_SECONDS+1, ChronoUnit.SECONDS));
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
}
