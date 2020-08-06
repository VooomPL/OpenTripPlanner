package org.opentripplanner.routing.edgetype.rentedgetype;

import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.geom.CoordinateXY;
import org.opentripplanner.routing.core.*;
import org.opentripplanner.routing.core.vehicle_sharing.*;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.vertextype.TemporaryRentVehicleVertex;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RentVehicleEdgeTest {

    private static final CarDescription CAR_1 = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"));
    private static final CarDescription CAR_2 = new CarDescription("2", 0, 0, FuelType.FOSSIL, Gearbox.MANUAL, new Provider(2, "PANEK"));

    private ParkingZoneInfo parkingZones;
    private ParkingZoneInfo parkingZonesEnabled;
    private RoutingRequest request;
    private State state, rentingState;

    private RentVehicleEdge edge;

    @Before
    public void setUp() {
        Graph graph = new Graph();
        TemporaryRentVehicleVertex vertex = new TemporaryRentVehicleVertex("v_name", new CoordinateXY(1, 2), "name");
        request = new RoutingRequest();
        request.setDummyRoutingContext(graph);
        request.setModes(new TraverseModeSet(TraverseMode.WALK, TraverseMode.CAR));
        request.setStartingMode(TraverseMode.WALK);
        request.vehicleValidator = mock(VehicleValidator.class);
        state = new State(vertex, request);
        parkingZones = mock(ParkingZoneInfo.class);
        parkingZonesEnabled = mock(ParkingZoneInfo.class);
        edge = new RentVehicleEdge(vertex, CAR_1, parkingZones, parkingZonesEnabled);
        StateEditor se = state.edit(edge);
        se.beginVehicleRenting(CAR_2);
        rentingState = se.makeState();
    }

    @Test
    public void shouldNotTraverseWhenRentingNotAllowed() {
        // when
        State traversed = edge.traverse(state);

        // then
        assertNull(traversed);
    }

    @Test
    public void shouldTraverseVehicle() {
        // given
        request.rentingAllowed = true;
        when(request.vehicleValidator.isValid(CAR_1)).thenReturn(true);

        // when
        State traversed = edge.traverse(state);

        // then
        assertNotNull(traversed);
        assertEquals(TraverseMode.CAR, traversed.getNonTransitMode());
        assertEquals(CAR_1, traversed.getCurrentVehicle());
    }

    @Test
    public void shouldReturnNullWhenVehicleDoesNotMatchCriteria() {
        // given
        request.rentingAllowed = true;
        when(request.vehicleValidator.isValid(CAR_1)).thenReturn(false);

        // when
        State traversed = edge.traverse(state);

        // then
        assertNull(traversed);
    }

    @Test
    public void shouldNotAllowToDropoffVehicleOutsideParkingZone() {
        // given
        request.rentingAllowed = true;
        when(request.vehicleValidator.isValid(CAR_1)).thenReturn(true);
        when(parkingZonesEnabled.appliesToVehicle(CAR_2)).thenReturn(true);
        when(parkingZones.appliesToVehicle(CAR_2)).thenReturn(false);

        // when
        State traversed = edge.traverse(rentingState);

        // then
        assertNull(traversed);
    }

    @Test
    public void shouldAllowToDropoffVehicleInsideParkingZone() {
        // given
        request.rentingAllowed = true;
        when(request.vehicleValidator.isValid(CAR_1)).thenReturn(true);
        when(parkingZonesEnabled.appliesToVehicle(CAR_2)).thenReturn(true);
        when(parkingZones.appliesToVehicle(CAR_2)).thenReturn(true);

        // when
        State traversed = edge.traverse(rentingState);

        // then
        assertNotNull(traversed);
    }

    @Test
    public void shouldAllowToDropoffVehicleWhenParkingZonesDoesNotApplyToIt() {
        // given
        request.rentingAllowed = true;
        when(request.vehicleValidator.isValid(CAR_1)).thenReturn(true);
        when(parkingZonesEnabled.appliesToVehicle(CAR_2)).thenReturn(false);

        // when
        State traversed = edge.traverse(rentingState);

        // then
        assertNotNull(traversed);
    }
}
