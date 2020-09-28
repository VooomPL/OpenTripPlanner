package org.opentripplanner.routing.spt.DominanceFunction;

import org.opentripplanner.routing.core.State;

/**
 * This approach is more coherent in Analyst when we are extracting travel times from the optimal
 * paths. It also leads to less branching and faster response times when building large shortest path trees.
 */
public class EarliestArrival extends DominanceFunction {
    /**
     * Return true if the first state has lower elapsed time than the second state.
     */
    @Override
    public boolean betterOrEqual(State a, State b) {
        return a.getElapsedTimeSeconds() <= b.getElapsedTimeSeconds();
    }
}
