package org.opentripplanner.routing.algorithm.costs;

public interface CostFunction {

    enum CostCategory {ORIGINAL, PRICE_ASSOCIATED}

    double getCostWeight(CostCategory category);

}
