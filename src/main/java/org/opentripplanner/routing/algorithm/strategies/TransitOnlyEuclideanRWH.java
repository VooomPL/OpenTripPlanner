package org.opentripplanner.routing.algorithm.strategies;

import org.opentripplanner.common.geometry.SphericalDistanceLibrary;
import org.opentripplanner.routing.algorithm.GenericDijkstra;
import org.opentripplanner.routing.algorithm.TraverseVisitor;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.edgetype.FreeEdge;
import org.opentripplanner.routing.edgetype.StreetTransitLink;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.vertextype.TransitStationStop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Euclidean remaining weight strategy that takes into account transit boarding costs where applicable.
 */
public class TransitOnlyEuclideanRWH implements RemainingWeightHeuristic {

    private static final Logger LOG = LoggerFactory.getLogger(TransitOnlyEuclideanRWH.class);

    private double lat;
    private double lon;
    private double maxTransitSpeed;
    private double requiredWalkDistance;
    private double walkMultiplier;
    private double streetWeight;

    @Override
    public void initialize(RoutingRequest options, long abortTime) {
        Vertex destination = getDestination(options.rctx.target);
        lat = destination.getLat();
        lon = destination.getLon();

        requiredWalkDistance = determineRequiredWalkDistance(options);
        maxTransitSpeed = options.getTransitSpeedUpperBound();
        walkMultiplier = options.routingReluctances.getWalkReluctance() / options.walkSpeed;
        streetWeight = walkMultiplier * requiredWalkDistance;
    }

    private Vertex getDestination(Vertex target) {
        if (target.getDegreeIn() == 1) {
            Edge edge = target.getOutgoing().stream().findFirst().get();
            if (edge instanceof FreeEdge) {
                return edge.getFromVertex();
            }
        }
        return target;
    }

    /**
     * On a transit trip, there are two cases:
     * (1) we're not on a transit vehicle. In this case, there are two possible ways to compute
     * the remaining distance, and we take whichever is smaller:
     * (a) walking distance / walking speed
     * (b) boarding cost + transit distance / transit speed (this is complicated a bit when
     * we know that there is some walking portion of the trip).
     * (2) we are on a transit vehicle, in which case the remaining weight is simply transit
     * distance / transit speed (no need for boarding cost), again considering any mandatory
     * walking.
     */
    @Override
    public double estimateRemainingWeight(State s) {
        Vertex sv = s.getVertex();
        double euclideanDistance = SphericalDistanceLibrary.fastDistance(sv.getLat(), sv.getLon(), lat, lon);
        if (euclideanDistance < requiredWalkDistance) {
            return walkMultiplier * euclideanDistance;
        }
        /* Due to the above conditional, the following value is known to be positive. */
        double transitWeight = (euclideanDistance - requiredWalkDistance) / maxTransitSpeed;
        return transitWeight + streetWeight;
    }

    @Override
    public void reset() {
    }

    @Override
    public void doSomeWork() {
    }

    /**
     * Figure out the minimum amount of walking to reach the destination from transit.
     * This is done by doing a Dijkstra search for the first reachable transit stop.
     */
    private double determineRequiredWalkDistance(final RoutingRequest req) {
        RoutingRequest options = req.clone();
        options.setArriveBy(!req.arriveBy);
        options.setRoutingContext(req.rctx.graph, req.rctx.fromVertex, req.rctx.toVertex);
        GenericDijkstra gd = new GenericDijkstra(options);
        State s = new State(options);
        gd.setHeuristic(new TrivialRemainingWeightHeuristic());
        final ClosestStopTraverseVisitor visitor = new ClosestStopTraverseVisitor();
        gd.traverseVisitor = visitor;
        gd.searchTerminationStrategy = (origin, target, current, spt, traverseOptions) -> visitor.distanceToClosestStop != Double.POSITIVE_INFINITY;
        gd.getShortestPathTree(s);
        return visitor.distanceToClosestStop;
    }

    private class ClosestStopTraverseVisitor implements TraverseVisitor {
        private double distanceToClosestStop = Double.POSITIVE_INFINITY;

        @Override
        public void visitEdge(Edge edge, State state) {
        }

        @Override
        public void visitEnqueue(State state) {
        }

        @Override
        public void visitVertex(State state) {
            Edge backEdge = state.getBackEdge();

            if (backEdge instanceof StreetTransitLink) {
                Vertex backVertex = state.getBackState().getVertex();
                distanceToClosestStop = SphericalDistanceLibrary.fastDistance(
                        backVertex.getLat(), backVertex.getLon(), lat, lon);
                LOG.debug("Found closest stop to search target: {} at {}m",
                        state.getVertex(), (int) distanceToClosestStop);
            } else if (state.getVertex() instanceof TransitStationStop && backEdge == null) {
                LOG.debug("Search target is a transit stop, no walking is required at end of trip");
                distanceToClosestStop = 0;
            }
        }
    }
}
