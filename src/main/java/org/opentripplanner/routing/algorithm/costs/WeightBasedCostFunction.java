package org.opentripplanner.routing.algorithm.costs;

import org.opentripplanner.routing.core.State;

public class WeightBasedCostFunction extends CostFunction{

    static {
        CostFunction.register("weightBased", WeightBasedCostFunction.class);
    }

    @Override
    public double getCost(State state) {
        return state.getWeight();
    }

}
