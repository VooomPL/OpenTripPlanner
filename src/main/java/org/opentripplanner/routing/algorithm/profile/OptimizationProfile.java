package org.opentripplanner.routing.algorithm.profile;

import org.opentripplanner.routing.algorithm.costs.CostFunction;
import org.opentripplanner.routing.algorithm.strategies.RemainingWeightHeuristic;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.spt.DominanceFunction;

public interface OptimizationProfile {

    CostFunction getCostFunction(RoutingRequest options);

    DominanceFunction getDominanceFunction(RoutingRequest options);

    RemainingWeightHeuristic getHeuristic(RoutingRequest options);

    RemainingWeightHeuristic getReversedSearchHeuristic(RoutingRequest options);

}
