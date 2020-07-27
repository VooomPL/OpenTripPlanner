package org.opentripplanner.graph_builder.linking;

import jersey.repackaged.com.google.common.collect.Lists;
import org.locationtech.jts.geom.Envelope;
import org.opentripplanner.common.geometry.HashGridSpatialIndex;
import org.opentripplanner.common.geometry.SphericalDistanceLibrary;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Vertex;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.opentripplanner.graph_builder.linking.LinkingGeoTools.RADIUS_DEG;

/**
 * Finds all closest edges in graph that given vertex could be linked to
 */
public class EdgesToLinkFinder {

    /**
     * If there are two ways and the distances to them differ by less than this value, we link to both of them
     */
    public static final double DUPLICATE_WAY_EPSILON_DEGREES = SphericalDistanceLibrary.metersToDegrees(0.001);

    private final HashGridSpatialIndex<Edge> idx;

    private final LinkingGeoTools linkingGeoTools;

    public EdgesToLinkFinder(HashGridSpatialIndex<Edge> idx, LinkingGeoTools linkingGeoTools) {
        this.idx = idx;
        this.linkingGeoTools = linkingGeoTools;
    }

    /**
     * Finds all near edges that we should link given vertex to
     */
    public List<StreetEdge> findEdgesToLink(Vertex vertex, TraverseMode traverseMode) {
        Envelope env = linkingGeoTools.createEnvelope(vertex);

        List<StreetEdge> candidateEdges = getCandidateEdges(env, traverseMode);

        List<Map.Entry<StreetEdge, Double>> sortedCandidateEdges = getSortedCandidateEdges(candidateEdges, vertex);

        if (sortedCandidateEdges.isEmpty() || sortedCandidateEdges.get(0).getValue() > RADIUS_DEG) {
            return emptyList();
        }

        return getBestEdges(sortedCandidateEdges);
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

    /**
     * Returns candidate edges with distances to given vertex sorted by this distance
     */
    private List<Map.Entry<StreetEdge, Double>> getSortedCandidateEdges(List<StreetEdge> candidateEdges, Vertex vertex) {
        return candidateEdges.stream()
                .collect(Collectors.toMap(Function.identity(), (edge) -> linkingGeoTools.distance(vertex, edge)))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(toList());
    }

    private List<StreetEdge> getBestEdges(List<Map.Entry<StreetEdge, Double>> sortedCandidateEdges) {
        // Find the best edges
        List<StreetEdge> bestEdges = Lists.newArrayList();

        // Add edges until there is a break of epsilon meters.
        // We do this to enforce determinism. if there are a lot of edges that are all extremely close to each other,
        // We want to be sure that we deterministically link to the same ones every time. Any hard cutoff means things can
        // fall just inside or beyond the cutoff depending on floating-point operations.
        int i = 0;
        do {
            bestEdges.add(sortedCandidateEdges.get(i++).getKey());
        } while (i < sortedCandidateEdges.size() && sortedCandidateEdges.get(i).getValue() - sortedCandidateEdges.get(i - 1).getValue() < DUPLICATE_WAY_EPSILON_DEGREES);

        return bestEdges;
    }
}
