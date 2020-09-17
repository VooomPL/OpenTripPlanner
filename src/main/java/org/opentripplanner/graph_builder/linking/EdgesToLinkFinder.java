package org.opentripplanner.graph_builder.linking;

import org.opentripplanner.common.geometry.HashGridSpatialIndex;
import org.opentripplanner.common.geometry.SphericalDistanceLibrary;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleDescription;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.util.I18NString;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * Finds all closest edges in graph that given vertex could be linked to
 */
public class EdgesToLinkFinder {

    protected static final double MAX_DISTANCE_FOR_VERTEX_NAME = SphericalDistanceLibrary.metersToDegrees(100);

    private final HashGridSpatialIndex<Edge> idx;

    private final LinkingGeoTools linkingGeoTools;

    private final BestCandidatesGetter bestCandidatesGetter;

    public EdgesToLinkFinder(HashGridSpatialIndex<Edge> idx, LinkingGeoTools linkingGeoTools,
                             BestCandidatesGetter bestCandidatesGetter) {
        this.idx = idx;
        this.linkingGeoTools = linkingGeoTools;
        this.bestCandidatesGetter = bestCandidatesGetter;
    }

    /**
     * Finds all near edges that we should link given vertex to
     */
    public List<StreetEdge> findEdgesToLink(Vertex vertex, TraverseMode traverseMode) {
        List<StreetEdge> candidateEdges = getCandidateEdges(vertex, traverseMode);
        return bestCandidatesGetter.getBestCandidates(candidateEdges, edge -> linkingGeoTools.distance(vertex, edge));
    }

    /**
     * Finds all near edges that we should link given vertex to. Return only edges that are traversable by given
     * renting vehicle.
     */
    public List<StreetEdge> findEdgesToLinkVehicle(Vertex vertex, VehicleDescription vehicle) {
        List<StreetEdge> candidateEdges =
                filterEdgesForGivenVehicle(getCandidateEdges(vertex, vehicle.getTraverseMode()), vehicle);
        return bestCandidatesGetter.getBestCandidates(candidateEdges, edge -> linkingGeoTools.distance(vertex, edge));
    }

    /**
     * Finds proper (non bogus) name for a given vertex. Returns a closest street name that is closer than
     * `MAX_DISTANCE_FOR_VERTEX_NAME` or empty optional if there is no such street name.
     */
    public Optional<I18NString> findNameForVertex(Vertex vertex) {
        List<StreetEdge> collect = idx.query(linkingGeoTools.createEnvelope(vertex)).stream()
                .filter(StreetEdge.class::isInstance)
                .map(StreetEdge.class::cast)
                .filter(edge -> !edge.hasBogusName())
                .collect(toList());
        List<StreetEdge> bestCandidates = bestCandidatesGetter.getBestCandidates(
                collect, edge -> linkingGeoTools.distance(vertex, edge), MAX_DISTANCE_FOR_VERTEX_NAME);
        return bestCandidates.stream()
                .findFirst()
                .map(StreetEdge::getRawName);
    }

    private List<StreetEdge> filterEdgesForGivenVehicle(List<StreetEdge> streetEdges, VehicleDescription vehicle) {
        return streetEdges.stream()
                .filter(edge -> edge.canTraverse(vehicle))
                .collect(toList());
    }

    private List<StreetEdge> getCandidateEdges(Vertex vertex, TraverseMode traverseMode) {
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
        return idx.query(linkingGeoTools.createEnvelope(vertex)).stream()
                .filter(StreetEdge.class::isInstance)
                .map(StreetEdge.class::cast)
                // note: not filtering by radius here as distance calculation is expensive
                // we do that below.
                .filter(edge -> edge.canTraverse(traverseModeSet) &&
                        // only link to edges still in the graph.
                        edge.getToVertex().getIncoming().contains(edge))
                .collect(toList());
    }
}
