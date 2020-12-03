package org.opentripplanner.routing.algorithm.profile;

import org.opentripplanner.routing.algorithm.costs.CostFunction;
import org.opentripplanner.routing.algorithm.costs.OriginalCostFunction;
import org.opentripplanner.routing.algorithm.strategies.*;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.spt.DominanceFunction;

public class MultimodalHeuristicProfile implements OptimizationProfile {

    private final CostFunction costFunction;
    private final DominanceFunction dominanceFunction;
    private final RemainingWeightHeuristic heuristic;
    private final RemainingWeightHeuristic reversedSearchHeuristic;

    public MultimodalHeuristicProfile(RoutingRequest request) {
        this.costFunction = new OriginalCostFunction();
        this.dominanceFunction = new DominanceFunction.EarliestArrival();
        if (request.disableRemainingWeightHeuristic) {
            heuristic = new TrivialRemainingWeightHeuristic();
            reversedSearchHeuristic = new TrivialRemainingWeightHeuristic();
        } else if (request.modes.isTransit() && !request.modes.getCar() && !request.modes.getBicycle()) {
            // Only use the BiDi heuristic for transit. It is not very useful for on-street modes.
            // heuristic = new InterleavedBidirectionalHeuristic(options.rctx.graph);
            // Use a simplistic heuristic until BiDi heuristic is improved, see #2153
            heuristic = new InterleavedBidirectionalHeuristic();
            reversedSearchHeuristic = new InterleavedBidirectionalHeuristic();
        } else {
            heuristic = new MultimodalHeuristic();
            reversedSearchHeuristic = new EuclideanRemainingWeightHeuristic();
        }
    }

    @Override
    public CostFunction getCostFunction() {
        return costFunction;
    }

    @Override
    public DominanceFunction getDominanceFunction() {
        return dominanceFunction;
    }

    @Override
    public RemainingWeightHeuristic getHeuristic() {
        return heuristic;
    }

    @Override
    public RemainingWeightHeuristic getReversedSearchHeuristic() {
        return reversedSearchHeuristic;
    }
}
