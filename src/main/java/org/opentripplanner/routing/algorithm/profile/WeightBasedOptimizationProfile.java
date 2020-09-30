package org.opentripplanner.routing.algorithm.profile;

import org.opentripplanner.routing.algorithm.costs.CostFunction;
import org.opentripplanner.routing.algorithm.costs.WeightBasedCostFunction;
import org.opentripplanner.routing.algorithm.strategies.EuclideanRemainingWeightHeuristic;
import org.opentripplanner.routing.algorithm.strategies.InterleavedBidirectionalHeuristic;
import org.opentripplanner.routing.algorithm.strategies.RemainingWeightHeuristic;
import org.opentripplanner.routing.algorithm.strategies.TrivialRemainingWeightHeuristic;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.spt.DominanceFunction;

public class WeightBasedOptimizationProfile implements OptimizationProfile {

    @Override
    public CostFunction getCostFunction(RoutingRequest options) {
        return new WeightBasedCostFunction();
    }

    @Override
    public DominanceFunction getDominanceFunction(RoutingRequest options) {
        return new DominanceFunction.EarliestArrival();
    }

    @Override
    public RemainingWeightHeuristic getHeuristic(RoutingRequest options) {
        RemainingWeightHeuristic heuristic;

        if (options.disableRemainingWeightHeuristic) {
            heuristic = new TrivialRemainingWeightHeuristic();
        } else if (options.modes.isTransit() && !options.modes.getCar() && !options.modes.getBicycle()) {
            // Only use the BiDi heuristic for transit. It is not very useful for on-street modes.
            // heuristic = new InterleavedBidirectionalHeuristic(options.rctx.graph);
            // Use a simplistic heuristic until BiDi heuristic is improved, see #2153
            heuristic = new InterleavedBidirectionalHeuristic();
        } else {
            heuristic = new EuclideanRemainingWeightHeuristic();
        }

        return heuristic;
    }

    @Override
    public RemainingWeightHeuristic getReversedSearchHeuristic(RoutingRequest options) {
        RemainingWeightHeuristic reversedSearchHeuristic;

        if (options.disableRemainingWeightHeuristic) {
            reversedSearchHeuristic = new TrivialRemainingWeightHeuristic();
        } else if (options.modes.isTransit() && !options.modes.getCar() && !options.modes.getBicycle()) {
            // Only use the BiDi heuristic for transit. It is not very useful for on-street modes.
            // heuristic = new InterleavedBidirectionalHeuristic(options.rctx.graph);
            // Use a simplistic heuristic until BiDi heuristic is improved, see #2153
            reversedSearchHeuristic = new InterleavedBidirectionalHeuristic();
        } else {
            reversedSearchHeuristic = new EuclideanRemainingWeightHeuristic();
        }

        return reversedSearchHeuristic;
    }
}
