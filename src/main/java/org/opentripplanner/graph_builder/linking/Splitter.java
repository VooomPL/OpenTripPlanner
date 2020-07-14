package org.opentripplanner.graph_builder.linking;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.linearref.LinearLocation;
import org.opentripplanner.common.geometry.HashGridSpatialIndex;
import org.opentripplanner.common.model.P2;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.vertextype.SplitterVertex;
import org.opentripplanner.routing.vertextype.TemporarySplitterVertex;

public class Splitter {

    private final Graph graph;

    private final HashGridSpatialIndex<Edge> idx;

    public Splitter(Graph graph, HashGridSpatialIndex<Edge> idx) {
        this.graph = graph;
        this.idx = idx;
    }

    /**
     * Split the street edge at the given fraction
     *
     * @param edge           to be split
     * @param ll             fraction at which to split the edge
     * @return Splitter vertex with added new edges
     */
    public SplitterVertex splitPermanently(StreetEdge edge, LinearLocation ll) {
        LineString geometry = edge.getGeometry();

        // create the geometries
        Coordinate splitPoint = ll.getCoordinate(geometry);

        // every edge can be split exactly once, so this is a valid label
        SplitterVertex v = new SplitterVertex(graph, "split from " + edge.getId(), splitPoint.x, splitPoint.y, edge);

        // Split the 'edge' at 'v' in 2 new edges and connect these 2 edges to the
        // existing vertices
        P2<StreetEdge> edges = edge.split(v, true);

        // update indices of new edges
        idx.insert(edges.first.getGeometry(), edges.first);
        idx.insert(edges.second.getGeometry(), edges.second);

        // (no need to remove original edge, we filter it when it comes out of the index)

        // remove original edge from the graph
        edge.getToVertex().removeIncoming(edge);
        edge.getFromVertex().removeOutgoing(edge);

        return v;
    }

    /**
     * Split the street edge at the given fraction
     *
     * @param edge           to be split
     * @param ll             fraction at which to split the edge
     * @param endVertex      if this is temporary edge this is true if this is end vertex otherwise it doesn't matter
     * @return Splitter vertex with added new edges
     */
    public SplitterVertex splitTemporarily(StreetEdge edge, LinearLocation ll, boolean endVertex) {
        LineString geometry = edge.getGeometry();

        // create the geometries
        Coordinate splitPoint = ll.getCoordinate(geometry);

        // every edge can be split exactly once, so this is a valid label
        SplitterVertex v = new TemporarySplitterVertex("split from " + edge.getId(), splitPoint.x, splitPoint.y, edge, endVertex);

        // Split the 'edge' at 'v' in 2 new edges and connect these 2 edges to the
        // existing vertices
        edge.split(v, false);
        return v;
    }
}
