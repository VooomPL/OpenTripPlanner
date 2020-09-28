package org.opentripplanner.routing.spt.DominanceFunction;

import org.opentripplanner.routing.core.State;

public class MinimumWeight extends DominanceFunction {
    /**
     * Return true if the first state has lower weight than the second state.
     */
    @Override
    public boolean betterOrEqual(State a, State b) {
        return a.weight <= b.weight;
    }
}
