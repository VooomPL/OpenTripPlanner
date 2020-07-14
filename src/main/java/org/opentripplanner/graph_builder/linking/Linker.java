package org.opentripplanner.graph_builder.linking;

import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.linearref.LinearLocation;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.location.TemporaryStreetLocation;
import org.opentripplanner.routing.vertextype.StreetVertex;

import java.util.List;

public class Linker {

    private final OnEdgeLinker onEdgeLinker;

    private final CandidateEdgesProvider candidateEdgesProvider;

    private final LinkingGeoTools linkingGeoTools;

    private final EdgesMaker edgesMaker;

    public Linker(OnEdgeLinker onEdgeLinker, CandidateEdgesProvider candidateEdgesProvider,
                  LinkingGeoTools linkingGeoTools, EdgesMaker edgesMaker) {
        this.onEdgeLinker = onEdgeLinker;
        this.candidateEdgesProvider = candidateEdgesProvider;
        this.linkingGeoTools = linkingGeoTools;
        this.edgesMaker = edgesMaker;
    }

    /**
     * Link this vertex into the graph
     */
    public boolean linkTemporarily(TemporaryStreetLocation vertex, TraverseMode traverseMode, RoutingRequest options) {
        List<StreetEdge> streetEdges = candidateEdgesProvider.getEdgesToLink(vertex, traverseMode);
        if (streetEdges.isEmpty()) {
            return false;
        }
        for (StreetEdge edge : streetEdges) {
            linkTemporarily(vertex, edge, options);
        }
        return true;
    }

    /**
     * split the edge and link in the transit stop
     */
    private void linkTemporarily(TemporaryStreetLocation tstop, StreetEdge edge, RoutingRequest options) {
        // TODO: we've already built this line string, we should save it
        LineString orig = edge.getGeometry();
        LinearLocation ll = linkingGeoTools.createLinearLocation(tstop, orig);
        if (tryLinkVertexToVertexTemporarily(tstop, edge, orig, ll)) {
            return;
        }
        //This throws runtime TrivialPathException if same edge is split in origin and destination link
        //It is only used in origin/destination linking since otherwise options is null
        if (options != null) {
            options.canSplitEdge(edge);
        }
        onEdgeLinker.linkVertexOnEdgeTemporarily(tstop, edge, ll);
    }

    /**
     * Link this vertex into the graph
     */
    public boolean linkPermanently(Vertex vertex, TraverseMode traverseMode) {
        List<StreetEdge> streetEdges = candidateEdgesProvider.getEdgesToLink(vertex, traverseMode);
        if (streetEdges.isEmpty()) {
            return false;
        }
        for (StreetEdge edge : streetEdges) {
            linkPermanently(vertex, edge);
        }
        return true;
    }

    /**
     * split the edge and link in the transit stop
     */
    private void linkPermanently(Vertex tstop, StreetEdge edge) {
        // TODO: we've already built this line string, we should save it
        LineString orig = edge.getGeometry();
        LinearLocation ll = linkingGeoTools.createLinearLocation(tstop, orig);
        if (tryLinkVertexToVertexPermanently(tstop, edge, orig, ll)) {
            return;
        }
        onEdgeLinker.linkVertexOnEdgePermanently(tstop, edge, ll);
    }

    private boolean tryLinkVertexToVertexTemporarily(TemporaryStreetLocation tstop, StreetEdge edge, LineString orig, LinearLocation ll) {
        // if we're very close to one end of the line or the other, or endwise, don't bother to split,
        // cut to the chase and link directly
        // We use a really tiny epsilon here because we only want points that actually snap to exactly the same location on the
        // street to use the same vertices. Otherwise the order the stops are loaded in will affect where they are snapped.
        if (ll.getSegmentIndex() == 0 && ll.getSegmentFraction() < 1e-8) {
            edgesMaker.makeTemporaryEdges(tstop, edge.getFromVertex());
            return true;
        }
        // -1 converts from count to index. Because of the fencepost problem, npoints - 1 is the "segment"
        // past the last point
        else if (ll.getSegmentIndex() == orig.getNumPoints() - 1) {
            edgesMaker.makeTemporaryEdges(tstop, edge.getToVertex());
            return true;
        }

        // nPoints - 2: -1 to correct for index vs count, -1 to account for fencepost problem
        else if (ll.getSegmentIndex() == orig.getNumPoints() - 2 && ll.getSegmentFraction() > 1 - 1e-8) {
            edgesMaker.makeTemporaryEdges(tstop, edge.getToVertex());
            return true;
        }
        return false;
    }

    private boolean tryLinkVertexToVertexPermanently(Vertex tstop, StreetEdge edge, LineString orig, LinearLocation ll) {
        // if we're very close to one end of the line or the other, or endwise, don't bother to split,
        // cut to the chase and link directly
        // We use a really tiny epsilon here because we only want points that actually snap to exactly the same location on the
        // street to use the same vertices. Otherwise the order the stops are loaded in will affect where they are snapped.
        if (ll.getSegmentIndex() == 0 && ll.getSegmentFraction() < 1e-8) {
            edgesMaker.makePermanentEdges(tstop, (StreetVertex) edge.getFromVertex());
            return true;
        }
        // -1 converts from count to index. Because of the fencepost problem, npoints - 1 is the "segment"
        // past the last point
        else if (ll.getSegmentIndex() == orig.getNumPoints() - 1) {
            edgesMaker.makePermanentEdges(tstop, (StreetVertex) edge.getToVertex());
            return true;
        }

        // nPoints - 2: -1 to correct for index vs count, -1 to account for fencepost problem
        else if (ll.getSegmentIndex() == orig.getNumPoints() - 2 && ll.getSegmentFraction() > 1 - 1e-8) {
            edgesMaker.makePermanentEdges(tstop, (StreetVertex) edge.getToVertex());
            return true;
        }
        return false;
    }

    public void setAddExtraEdgesToAreas(boolean addExtraEdgesToAreas) {
        onEdgeLinker.setAddExtraEdgesToAreas(addExtraEdgesToAreas);
    }
}
