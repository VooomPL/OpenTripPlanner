package org.opentripplanner.routing.algorithm.costs;

import org.opentripplanner.routing.core.State;

public class WeightBasedCostFunction implements CostFunction{

    @Override
    public double getCost(State state) {
        return state.getWeight();
    }

}
