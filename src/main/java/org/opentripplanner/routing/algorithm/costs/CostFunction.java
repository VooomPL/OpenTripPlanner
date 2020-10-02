package org.opentripplanner.routing.algorithm.costs;

public interface CostFunction {

    enum CostCategory {ORIGINAL}

    double getCostWeight(CostCategory category, double cost);

}
