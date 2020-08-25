package org.opentripplanner.routing.edgetype.rentedgetype;

import org.junit.Before;
import org.junit.Test;
import org.opentripplanner.routing.core.*;
import org.opentripplanner.routing.core.vehicle_sharing.CarDescription;
import org.opentripplanner.routing.core.vehicle_sharing.FuelType;
import org.opentripplanner.routing.core.vehicle_sharing.Gearbox;
import org.opentripplanner.routing.core.vehicle_sharing.Provider;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.vertextype.IntersectionVertex;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DropoffVehicleEdgeTest {

    private static final CarDescription CAR_1 = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"));

    private DropoffVehicleEdge edgeWithoutParkingZones, edge;
    private RoutingRequest request;
    private State rentingState;
    private ParkingZoneInfo parkingZoneInfo;

    @Before
    public void setUp() {
        Graph graph = new Graph();
        IntersectionVertex v = new IntersectionVertex(graph, "v_name", 0, 0);
        parkingZoneInfo = mock(ParkingZoneInfo.class);
        edgeWithoutParkingZones = new DropoffVehicleEdge(v);
        edge = new DropoffVehicleEdge(v, parkingZoneInfo);
        request = new RoutingRequest();
        request.setDummyRoutingContext(graph);
        request.setModes(new TraverseModeSet(TraverseMode.WALK, TraverseMode.CAR));
        request.setStartingMode(TraverseMode.WALK);
        State state = new State(v, request);
        StateEditor se = state.edit(edge);
        se.beginVehicleRenting(CAR_1);
        rentingState = se.makeState();

    }

    @Test
    public void shouldReturnVehicleIfParkingZonesDisabled() {
        // when
        State traversed = edgeWithoutParkingZones.traverse(rentingState);

        // then
        assertNotNull(traversed);
        assertFalse(traversed.isCurrentlyRentingVehicle());
    }

    @Test
    public void shouldNotReturnVehicleIfCannotDropoffHere() {
        // given
        when(parkingZoneInfo.canDropoffVehicleHere(CAR_1)).thenReturn(false);

        // when
        State traversed = edge.traverse(rentingState);

        // then
        assertNull(traversed);
    }

    @Test
    public void shouldReturnVehicleIfCanDropoffHere() {
        // given
        request.rentingAllowed = true;
        when(parkingZoneInfo.canDropoffVehicleHere(CAR_1)).thenReturn(true);

        // when
        State traversed = edge.traverse(rentingState);

        // then
        assertNotNull(traversed);
        assertFalse(traversed.isCurrentlyRentingVehicle());
    }
}
