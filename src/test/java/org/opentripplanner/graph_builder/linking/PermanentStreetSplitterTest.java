package org.opentripplanner.graph_builder.linking;


import org.junit.Before;
import org.junit.Test;
import org.opentripplanner.model.FeedScopedId;
import org.opentripplanner.model.Stop;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.vertextype.OsmVertex;
import org.opentripplanner.routing.vertextype.TransitStop;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class PermanentStreetSplitterTest {

    private Graph graph;
    private VertexLinker vertexLinker;
    private PermanentStreetSplitter permanentStreetSplitter;

    private Stop stop;

    @Before
    public void setUp() {
        graph = new Graph();
        vertexLinker = mock(VertexLinker.class);
        permanentStreetSplitter = new PermanentStreetSplitter(graph, null, vertexLinker);

        stop = new Stop();
        stop.setName("transitVertex 1");
        stop.setLon(-74.005);
        stop.setLat(40.0099999);
        stop.setId(new FeedScopedId("A", "fleem station"));
    }

    @Test
    public void shouldLinkVertexToGraph() {
        // given
        TransitStop transitStop = new TransitStop(graph, stop);
        when(vertexLinker.linkPermanently(transitStop, TraverseMode.WALK)).thenReturn(true);

        // when
        boolean linked = permanentStreetSplitter.link(transitStop);

        // then
        assertTrue(linked);
        verify(vertexLinker, times(1)).linkPermanently(transitStop, TraverseMode.WALK);
        verifyNoMoreInteractions(vertexLinker);
    }

    @Test
    public void shouldLinkAllRelevantVerticesToGraph() {
        // given
        TransitStop transitStop = new TransitStop(graph, stop);
        when(vertexLinker.linkPermanently(transitStop, TraverseMode.WALK)).thenReturn(true);

        // when
        permanentStreetSplitter.link();

        // then
        verify(vertexLinker, times(1)).linkPermanently(transitStop, TraverseMode.WALK);
        verifyNoMoreInteractions(vertexLinker);
    }

    @Test
    public void shouldIgnoreLinkingFailure() {
        // given
        TransitStop transitStop = new TransitStop(graph, stop);
        when(vertexLinker.linkPermanently(transitStop, TraverseMode.WALK)).thenReturn(false);

        // when
        permanentStreetSplitter.link();

        // then
        verify(vertexLinker, times(1)).linkPermanently(transitStop, TraverseMode.WALK);
        verifyNoMoreInteractions(vertexLinker);
    }

    @Test
    public void shouldNotLinkNotRelevantVertices() {
        // given
        new OsmVertex(graph, "name", 1.1, 2.2, 1);

        // when
        permanentStreetSplitter.link();

        // then
        verifyZeroInteractions(vertexLinker);
    }
}
