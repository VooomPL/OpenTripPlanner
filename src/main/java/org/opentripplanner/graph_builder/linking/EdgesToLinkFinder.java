package org.opentripplanner.graph_builder.linking;

import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import org.locationtech.jts.geom.Envelope;
import org.opentripplanner.common.geometry.HashGridSpatialIndex;
import org.opentripplanner.common.geometry.SphericalDistanceLibrary;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Vertex;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.opentripplanner.graph_builder.linking.LinkingGeoTools.RADIUS_DEG;

/**
 * Finds all closest edges in graph that given vertex could be linked to
 */
public class EdgesToLinkFinder {

    /**
     * if there are two ways and the distances to them differ by less than this value, we link to both of them
     */
    public static final double DUPLICATE_WAY_EPSILON_DEGREES = SphericalDistanceLibrary.metersToDegrees(0.001);

    private final HashGridSpatialIndex<Edge> idx;

    private final LinkingGeoTools linkingGeoTools;

    public EdgesToLinkFinder(HashGridSpatialIndex<Edge> idx, LinkingGeoTools linkingGeoTools) {
        this.idx = idx;
        this.linkingGeoTools = linkingGeoTools;
    }

    /**
     * finds all near edges that we should link given vertex to
     */
    public List<StreetEdge> findEdgesToLink(Vertex vertex, TraverseMode traverseMode) {
        Envelope env = linkingGeoTools.createEnvelope(vertex);

        List<StreetEdge> candidateEdges = getCandidateEdges(env, traverseMode);

        // Make a map of distances to all edges.
        TIntDoubleMap distances = getDistances(candidateEdges, vertex);

        EdgesFinderUtils.sort(candidateEdges, distances, Edge::getId);

        // find the closest candidate edges
        if (candidateEdges.isEmpty() || distances.get(candidateEdges.get(0).getId()) > RADIUS_DEG) {
            return emptyList();
        }

        // find the best edges
        return EdgesFinderUtils.getBestCandidates(candidateEdges, distances, Edge::getId);
    }

    private List<StreetEdge> getCandidateEdges(Envelope env, TraverseMode traverseMode) {
        final TraverseModeSet traverseModeSet;
        if (traverseMode == TraverseMode.BICYCLE) {
            traverseModeSet = new TraverseModeSet(traverseMode, TraverseMode.WALK);
        } else {
            traverseModeSet = new TraverseModeSet(traverseMode);
        }
        // We sort the list of candidate edges by distance to the stop
        // This should remove any issues with things coming out of the spatial index in different orders
        // Then we link to everything that is within DUPLICATE_WAY_EPSILON_METERS of of the best distance
        // so that we capture back edges and duplicate ways.
        return idx.query(env).stream()
                .filter(StreetEdge.class::isInstance)
                .map(StreetEdge.class::cast)
                // note: not filtering by radius here as distance calculation is expensive
                // we do that below.
                .filter(edge -> edge.canTraverse(traverseModeSet) &&
                        // only link to edges still in the graph.
                        edge.getToVertex().getIncoming().contains(edge))
                .collect(toList());
    }

    private TIntDoubleMap getDistances(List<StreetEdge> candidateEdges, Vertex vertex) {
        TIntDoubleMap distances = new TIntDoubleHashMap();
        candidateEdges.forEach(e -> distances.put(e.getId(), linkingGeoTools.distance(vertex, e)));
        return distances;
    }
}
