package org.opentripplanner.graph_builder.linking;

import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.geom.CoordinateXY;
import org.opentripplanner.common.model.GenericLocation;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.routing.core.vehicle_sharing.CarDescription;
import org.opentripplanner.routing.core.vehicle_sharing.FuelType;
import org.opentripplanner.routing.core.vehicle_sharing.Gearbox;
import org.opentripplanner.routing.core.vehicle_sharing.Provider;
import org.opentripplanner.routing.edgetype.rentedgetype.ParkingZoneInfo;
import org.opentripplanner.routing.edgetype.rentedgetype.RentVehicleEdge;
import org.opentripplanner.routing.edgetype.rentedgetype.TemporaryDropoffVehicleEdge;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.location.StreetLocation;
import org.opentripplanner.routing.location.TemporaryStreetLocation;
import org.opentripplanner.routing.vertextype.TemporaryRentVehicleVertex;
import org.opentripplanner.updater.vehicle_sharing.parking_zones.ParkingZonesCalculator;
import org.opentripplanner.util.I18NString;
import org.opentripplanner.util.NonLocalizedString;

import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class TemporaryStreetSplitterTest {

    private static final CarDescription CAR = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"));

    private Graph graph;
    private ToStreetEdgeLinker toStreetEdgeLinker;
    private ToTransitStopLinker toTransitStopLinker;
    private EdgesToLinkFinder edgesToLinkFinder;

    private TemporaryStreetSplitter temporaryStreetSplitter;

    RoutingRequest routingRequest;
    GenericLocation genericLocation;

    @Before
    public void setUp() {
        graph = new Graph();

        toStreetEdgeLinker = mock(ToStreetEdgeLinker.class);
        toTransitStopLinker = mock(ToTransitStopLinker.class);
        edgesToLinkFinder = mock(EdgesToLinkFinder.class);
        temporaryStreetSplitter = new TemporaryStreetSplitter(graph, toStreetEdgeLinker, toTransitStopLinker, edgesToLinkFinder);

        genericLocation = new GenericLocation(10, 23);
        routingRequest = new RoutingRequest();
    }

    @Test
    public void shouldReturnClosestVertexWhenLinkingToEdgeSucceed() {
        // given
        when(toStreetEdgeLinker.linkTemporarily(any(), any(), eq(routingRequest))).thenReturn(true);

        // when
        TemporaryStreetLocation closestVertex = temporaryStreetSplitter.linkLocationToGraph(genericLocation, routingRequest, false);

        // then
        assertEquals(genericLocation.getCoordinate(), closestVertex.getCoordinate());
        assertFalse(closestVertex.isEndVertex());
        verify(toStreetEdgeLinker, times(1)).linkTemporarily(closestVertex, TraverseMode.WALK, routingRequest);
        verifyNoMoreInteractions(toStreetEdgeLinker);
        verifyZeroInteractions(toTransitStopLinker, edgesToLinkFinder);
    }

    @Test
    public void shouldReturnClosestVertexWhenLinkingToTransitStopSucceeded() {
        // given
        when(toStreetEdgeLinker.linkTemporarily(any(), any(), eq(routingRequest))).thenReturn(false);
        when(toTransitStopLinker.tryLinkVertexToStop(any())).thenReturn(true);

        // when
        TemporaryStreetLocation closestVertex = temporaryStreetSplitter.linkLocationToGraph(genericLocation, routingRequest, false);

        // then
        assertEquals(genericLocation.getCoordinate(), closestVertex.getCoordinate());
        verify(toStreetEdgeLinker, times(1)).linkTemporarily(closestVertex, TraverseMode.WALK, routingRequest);
        verify(toTransitStopLinker, times(1)).tryLinkVertexToStop(closestVertex);
        verifyNoMoreInteractions(toStreetEdgeLinker, toTransitStopLinker);
        verifyZeroInteractions(edgesToLinkFinder);
    }

    @Test
    public void shouldReturnNotLinkedVertexWhenAllLinkingFailed() {
        // given
        when(toStreetEdgeLinker.linkTemporarily(any(), any(), eq(routingRequest))).thenReturn(false);
        when(toTransitStopLinker.tryLinkVertexToStop(any())).thenReturn(false);

        // when
        TemporaryStreetLocation closestVertex = temporaryStreetSplitter.linkLocationToGraph(genericLocation, routingRequest, false);

        // then
        assertEquals(genericLocation.getCoordinate(), closestVertex.getCoordinate());
        verify(toStreetEdgeLinker, times(1)).linkTemporarily(closestVertex, TraverseMode.WALK, routingRequest);
        verify(toTransitStopLinker, times(1)).tryLinkVertexToStop(closestVertex);
        verifyNoMoreInteractions(toStreetEdgeLinker, toTransitStopLinker, edgesToLinkFinder);
        verifyZeroInteractions(edgesToLinkFinder);
    }

    @Test
    public void shouldSetTraverseModeToStartingModeWhenRoutingWithRentingVehicles() {
        // given
        routingRequest.startingMode = TraverseMode.WALK;
        when(toStreetEdgeLinker.linkTemporarily(any(), any(), eq(routingRequest))).thenReturn(true);

        // when
        TemporaryStreetLocation closestVertex = temporaryStreetSplitter.linkLocationToGraph(genericLocation, routingRequest, false);

        // then
        verify(toStreetEdgeLinker, times(1)).linkTemporarily(closestVertex, TraverseMode.WALK, routingRequest);
        verifyZeroInteractions(edgesToLinkFinder);
    }

    @Test
    public void shouldSetTraverseModeToCarWhenRoutingTaxi() {
        // given
        routingRequest.startingMode = TraverseMode.CAR;
        when(toStreetEdgeLinker.linkTemporarily(any(), any(), eq(routingRequest))).thenReturn(true);

        // when
        TemporaryStreetLocation closestVertex = temporaryStreetSplitter.linkLocationToGraph(genericLocation, routingRequest, false);

        // then
        verify(toStreetEdgeLinker, times(1)).linkTemporarily(closestVertex, TraverseMode.CAR, routingRequest);
        verifyNoMoreInteractions(toStreetEdgeLinker);
        verifyZeroInteractions(toTransitStopLinker, edgesToLinkFinder);
    }

    @Test
    public void shouldSetTraverseModeToWalkWhenEndOfParkAndRide() {
        // given
        routingRequest.parkAndRide = true;
        when(toStreetEdgeLinker.linkTemporarily(any(), any(), eq(routingRequest))).thenReturn(true);

        // when
        TemporaryStreetLocation closestVertex = temporaryStreetSplitter.linkLocationToGraph(genericLocation, routingRequest, true);

        // then
        verify(toStreetEdgeLinker, times(1)).linkTemporarily(closestVertex, TraverseMode.WALK, routingRequest);
        verifyZeroInteractions(edgesToLinkFinder);
    }

    @Test
    public void shouldSetTraverseModeToBicycleWhenRoutingBicycle() {
        // given
        routingRequest.modes = new TraverseModeSet(TraverseMode.BICYCLE);
        when(toStreetEdgeLinker.linkTemporarily(any(), any(), eq(routingRequest))).thenReturn(true);

        // when
        TemporaryStreetLocation closestVertex = temporaryStreetSplitter.linkLocationToGraph(genericLocation, routingRequest, false);

        // then
        verify(toStreetEdgeLinker, times(1)).linkTemporarily(closestVertex, TraverseMode.BICYCLE, routingRequest);
        verifyZeroInteractions(edgesToLinkFinder);
    }

    @Test
    public void shouldSetTraverseModeToCarWhenRoutingCar() {
        // given
        routingRequest.modes = new TraverseModeSet(TraverseMode.CAR, TraverseMode.WALK);
        when(toStreetEdgeLinker.linkTemporarily(any(), any(), eq(routingRequest))).thenReturn(true);

        // when
        TemporaryStreetLocation closestVertex = temporaryStreetSplitter.linkLocationToGraph(genericLocation, routingRequest, false);

        // then
        verify(toStreetEdgeLinker, times(1)).linkTemporarily(closestVertex, TraverseMode.CAR, routingRequest);
        verifyZeroInteractions(edgesToLinkFinder);
    }

    @Test
    public void shouldAddDropoffVehicleEdgeAtDestination() {
        // given
        when(toStreetEdgeLinker.linkTemporarily(any(), any(), eq(routingRequest))).thenReturn(true);

        graph.parkingZonesCalculator = mock(ParkingZonesCalculator.class);
        when(graph.parkingZonesCalculator.getParkingZonesForLocation(any())).thenReturn(new ParkingZoneInfo(emptyList(), emptyList(), emptyList()));

        // when
        TemporaryStreetLocation closestVertex = temporaryStreetSplitter.linkLocationToGraph(genericLocation, routingRequest, true);

        // then
        assertEquals(1, closestVertex.getOutgoing().size());
        Edge edge = closestVertex.getOutgoing().stream().findFirst().get();
        assertTrue(closestVertex.getIncoming().contains(edge));
        assertTrue(edge instanceof TemporaryDropoffVehicleEdge);
        verify(graph.parkingZonesCalculator, times(1)).getParkingZonesForLocation(closestVertex);
        verifyNoMoreInteractions(graph.parkingZonesCalculator);
        verifyZeroInteractions(toTransitStopLinker, edgesToLinkFinder);
    }

    @Test
    public void shouldNotAddDropoffVehicleEdgeAtVertexOtherThanDestination() {
        // given
        when(toStreetEdgeLinker.linkTemporarily(any(), any(), eq(routingRequest))).thenReturn(true);

        // when
        TemporaryStreetLocation closestVertex = temporaryStreetSplitter.linkLocationToGraph(genericLocation, routingRequest, false);

        // then
        assertTrue(closestVertex.getOutgoing().isEmpty());
        verifyZeroInteractions(edgesToLinkFinder);
    }

    @Test
    public void shouldReturnEmptyIfFailedToLinkVertex() {
        // given
        when(toStreetEdgeLinker.linkTemporarilyBothWays(any(), any())).thenReturn(false);

        // when
        Optional<TemporaryRentVehicleVertex> temporaryRentVehicleVertex = temporaryStreetSplitter.linkRentableVehicleToGraph(CAR);

        // then
        assertFalse(temporaryRentVehicleVertex.isPresent());
        verify(toStreetEdgeLinker, times(1)).linkTemporarilyBothWays(any(), eq(CAR));
        verifyNoMoreInteractions(toStreetEdgeLinker);
        verifyZeroInteractions(toTransitStopLinker, edgesToLinkFinder);
    }

    @Test
    public void shouldReturnVertexIfSucceededInLinking() {
        // given
        when(toStreetEdgeLinker.linkTemporarilyBothWays(any(), any())).thenReturn(true);

        // when
        Optional<TemporaryRentVehicleVertex> temporaryRentVehicleVertex = temporaryStreetSplitter.linkRentableVehicleToGraph(CAR);

        // then
        assertTrue(temporaryRentVehicleVertex.isPresent());
        TemporaryRentVehicleVertex vertex = temporaryRentVehicleVertex.get();
        assertEquals(1, vertex.getIncoming().size());
        assertEquals(1, vertex.getOutgoing().size());
        assertEquals(vertex.getIncoming(), vertex.getOutgoing());
        assertEquals(CAR.getLatitude(), vertex.getLat(), 0.1);
        assertEquals(CAR.getLongitude(), vertex.getLon(), 0.1);
        Edge edge = vertex.getOutgoing().stream().findFirst().get();
        assertTrue(edge instanceof RentVehicleEdge);
        RentVehicleEdge rentVehicleEdge = (RentVehicleEdge) edge;
        assertEquals(CAR, rentVehicleEdge.getVehicle());
        verify(toStreetEdgeLinker, times(1)).linkTemporarilyBothWays(vertex, CAR);
        verifyNoMoreInteractions(toStreetEdgeLinker);
        verifyZeroInteractions(toTransitStopLinker, edgesToLinkFinder);
    }

    @Test
    public void shouldAddParkingZonesForVehicleVertex() {
        // given
        graph.parkingZonesCalculator = mock(ParkingZonesCalculator.class);
        when(graph.parkingZonesCalculator.getParkingZonesForLocation(any())).thenReturn(new ParkingZoneInfo(emptyList(), emptyList(), emptyList()));
        when(toStreetEdgeLinker.linkTemporarilyBothWays(any(), any())).thenReturn(true);

        // when
        Optional<TemporaryRentVehicleVertex> temporaryRentVehicleVertex = temporaryStreetSplitter.linkRentableVehicleToGraph(CAR);

        // then
        assertTrue(temporaryRentVehicleVertex.isPresent());
        TemporaryRentVehicleVertex vertex = temporaryRentVehicleVertex.get();
        assertEquals(1, vertex.getIncoming().size());
        assertEquals(1, vertex.getOutgoing().size());
        assertEquals(vertex.getIncoming(), vertex.getOutgoing());
        Edge edge = vertex.getOutgoing().stream().findFirst().get();

        verify(graph.parkingZonesCalculator, times(1)).getParkingZonesForLocation(vertex);
        verify(toStreetEdgeLinker, times(1)).linkTemporarilyBothWays(vertex, CAR);
        verifyNoMoreInteractions(graph.parkingZonesCalculator, toStreetEdgeLinker);
        verifyZeroInteractions(toTransitStopLinker, edgesToLinkFinder);
    }

    @Test
    public void shouldReturnProperNameForVertex() {
        // given
        Vertex vertex = new StreetLocation("id", new CoordinateXY(1, 2), "bogus name");
        Optional<I18NString> someName = Optional.of(new NonLocalizedString("actual name"));
        when(edgesToLinkFinder.findNameForVertex(vertex)).thenReturn(someName);

        // when
        Optional<I18NString> returnedName = temporaryStreetSplitter.findNameForVertex(vertex);

        // then
        assertEquals(someName, returnedName);
        verify(edgesToLinkFinder, times(1)).findNameForVertex(vertex);
        verifyNoMoreInteractions(edgesToLinkFinder);
        verifyZeroInteractions(toStreetEdgeLinker, toTransitStopLinker);
    }
}
