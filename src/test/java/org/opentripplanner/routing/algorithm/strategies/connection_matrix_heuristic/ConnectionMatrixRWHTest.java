package org.opentripplanner.routing.algorithm.strategies.connection_matrix_heuristic;

import org.junit.Before;
import org.junit.Test;
import org.opentripplanner.common.model.GenericLocation;
import org.opentripplanner.routing.algorithm.strategies.RemainingWeightHeuristic;
import org.opentripplanner.routing.core.RoutingContext;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.impl.StreetVertexIndexServiceImpl;
import org.opentripplanner.routing.vertextype.IntersectionVertex;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConnectionMatrixRWHTest {

    private Graph graph;
    private RemainingWeightHeuristic heuristic;

    @Before
    public void setUp() {
        graph = new Graph();
        graph.streetIndex = new StreetVertexIndexServiceImpl(graph);
        graph.connectionMatrixHeuristicData = ConnectionMatrixHeuristicDataHelper.createData();
        RoutingRequest request = new RoutingRequest();
        request.to = new GenericLocation(0.5, 10.5);
        request.rctx = new RoutingContext(request, graph);
        heuristic = new ConnectionMatrixRWH();
        heuristic.initialize(request, Long.MAX_VALUE);
    }

    @Test
    public void shouldEstimateRemainingWeight() {
        // given
        State state = mock(State.class);
        when(state.getVertex()).thenReturn(new IntersectionVertex(graph, "label", 14.5, 2.5));

        // then
        assertEquals(15.0, heuristic.estimateRemainingWeight(state), 0.0);
    }
}