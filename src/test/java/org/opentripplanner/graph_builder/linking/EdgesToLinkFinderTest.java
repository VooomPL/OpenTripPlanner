package org.opentripplanner.graph_builder.linking;

import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.opentripplanner.common.geometry.GeometryUtils;
import org.opentripplanner.common.geometry.HashGridSpatialIndex;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.edgetype.StreetTraversalPermission;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.location.StreetLocation;
import org.opentripplanner.routing.vertextype.StreetVertex;

import java.util.List;

import static com.google.common.collect.ImmutableList.of;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EdgesToLinkFinderTest {

    private HashGridSpatialIndex<Edge> index;

    private EdgesToLinkFinder edgesToLinkFinder;

    private StreetVertex vertex;
    private StreetEdge edgeWalk, edgeCar1, edgeCar2, edgeCar3;

    @Before
    public void setUp() {
        index = mock(HashGridSpatialIndex.class);
        LinkingGeoTools linkingGeoTools = mock(LinkingGeoTools.class);
        edgesToLinkFinder = new EdgesToLinkFinder(index, linkingGeoTools);

        vertex = new StreetLocation("id1", new Coordinate(0, 1), "name");

        StreetVertex from = new StreetLocation("id2", new Coordinate(0, 1), "name");
        StreetVertex to = new StreetLocation("id3", new Coordinate(1, 1), "name");
        edgeWalk = new StreetEdge(from, to, GeometryUtils.makeLineString(0, 1, 0.5, 1, 1, 1),
                "S. Crystal Dr", 100, StreetTraversalPermission.PEDESTRIAN, false);
        edgeCar1 = new StreetEdge(from, to, GeometryUtils.makeLineString(0, 1, 0.5, 1, 1, 1),
                "S. Crystal Dr", 100, StreetTraversalPermission.CAR, false);
        edgeCar2 = new StreetEdge(from, to, GeometryUtils.makeLineString(0, 1, 0.5, 1, 1, 1),
                "S. Crystal Dr", 100, StreetTraversalPermission.CAR, false);
        edgeCar3 = new StreetEdge(from, to, GeometryUtils.makeLineString(0, 1, 0.5, 1, 1, 1),
                "S. Crystal Dr", 100, StreetTraversalPermission.CAR, false);

        when(linkingGeoTools.distance(vertex, edgeWalk)).thenReturn(0.0001);
        when(linkingGeoTools.distance(vertex, edgeCar1)).thenReturn(0.0002);
        when(linkingGeoTools.distance(vertex, edgeCar2)).thenReturn(0.0002000000001);
        when(linkingGeoTools.distance(vertex, edgeCar3)).thenReturn(0.0003);
    }

    @Test
    public void shouldReturnClosestEdge() {
        // given
        when(index.query(any())).thenReturn(singletonList(edgeWalk));

        // when
        List<StreetEdge> streetEdges = edgesToLinkFinder.findEdgesToLink(vertex, TraverseMode.WALK);

        // then
        assertEquals(1, streetEdges.size());
        assertTrue(streetEdges.contains(edgeWalk));
    }

    @Test
    public void shouldFilterEdgesBasedOnTraverseMode() {
        // given
        when(index.query(any())).thenReturn(singletonList(edgeCar1));

        // when
        List<StreetEdge> streetEdges = edgesToLinkFinder.findEdgesToLink(vertex, TraverseMode.WALK);

        // then
        assertEquals(0, streetEdges.size());
    }

    @Test
    public void shouldAllowWalkingBike() {
        // given
        when(index.query(any())).thenReturn(singletonList(edgeWalk));

        // when
        List<StreetEdge> streetEdges = edgesToLinkFinder.findEdgesToLink(vertex, TraverseMode.BICYCLE);

        // then
        assertEquals(1, streetEdges.size());
        assertTrue(streetEdges.contains(edgeWalk));
    }

    @Test
    public void shouldFilterEdgesBasedOnDistance() {
        // given
        when(index.query(any())).thenReturn(of(edgeCar1, edgeCar2, edgeCar3));

        // when
        List<StreetEdge> streetEdges = edgesToLinkFinder.findEdgesToLink(vertex, TraverseMode.CAR);

        // then
        assertEquals(2, streetEdges.size());
        assertTrue(streetEdges.contains(edgeCar1));
        assertTrue(streetEdges.contains(edgeCar2));
    }

    @Test
    public void shouldSortEdgesByDistane() {
        // given
        when(index.query(any())).thenReturn(of(edgeCar3, edgeCar2, edgeCar1));

        // when
        List<StreetEdge> streetEdges = edgesToLinkFinder.findEdgesToLink(vertex, TraverseMode.CAR);

        // then
        assertEquals(2, streetEdges.size());
        assertTrue(streetEdges.contains(edgeCar1));
        assertTrue(streetEdges.contains(edgeCar2));
    }
}
