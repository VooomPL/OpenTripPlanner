package org.opentripplanner.routing.algorithm.profile;

import org.opentripplanner.routing.algorithm.costs.CostFunction;
import org.opentripplanner.routing.algorithm.strategies.RemainingWeightHeuristic;
import org.opentripplanner.routing.spt.DominanceFunction;

public interface OptimizationProfile {

    CostFunction getCostFunction();

    DominanceFunction getDominanceFunction();

    RemainingWeightHeuristic getHeuristic();

    RemainingWeightHeuristic getReversedSearchHeuristic();

}
