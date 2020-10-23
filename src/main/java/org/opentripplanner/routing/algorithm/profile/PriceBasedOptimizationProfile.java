package org.opentripplanner.routing.algorithm.profile;

import org.opentripplanner.routing.algorithm.costs.ConfigurableWeightsCostFunction;
import org.opentripplanner.routing.algorithm.costs.CostFunction;
import org.opentripplanner.routing.algorithm.strategies.PriceBasedRemainingWeightHeuristic;
import org.opentripplanner.routing.algorithm.strategies.RemainingWeightHeuristic;
import org.opentripplanner.routing.spt.DominanceFunction;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class PriceBasedOptimizationProfile implements OptimizationProfile {

    private final CostFunction costFunction;
    private final DominanceFunction dominanceFunction;
    private final RemainingWeightHeuristic heuristic;
    private final RemainingWeightHeuristic reversedSearchHeuristic;

    public PriceBasedOptimizationProfile(Map<CostFunction.CostCategory, Double> costWeights) {
        this.costFunction = new ConfigurableWeightsCostFunction(Optional.ofNullable(costWeights).orElse(Collections.emptyMap()));
        this.dominanceFunction = new DominanceFunction.LowestPrice();
        this.heuristic = new PriceBasedRemainingWeightHeuristic(Optional.ofNullable(costWeights).orElse(Collections.emptyMap()));
        this.reversedSearchHeuristic = new PriceBasedRemainingWeightHeuristic(Optional.ofNullable(costWeights).orElse(Collections.emptyMap()));
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
