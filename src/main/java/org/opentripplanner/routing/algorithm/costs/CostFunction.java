package org.opentripplanner.routing.algorithm.costs;

import org.opentripplanner.routing.core.State;

public interface CostFunction {

    double getCost(State state);

}
