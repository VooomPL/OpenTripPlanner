package org.opentripplanner.routing.spt.DominanceFunction;

import org.opentripplanner.routing.core.State;

/**
 * A dominance function that prefers the least walking. This should only be used with walk-only searches because
 * it does not include any functions of time, and once transit is boarded walk distance is constant.
 * <p>
 * It is used when building stop tree caches for egress from transit stops.
 */
public class LeastWalk extends DominanceFunction {

    @Override
    protected boolean betterOrEqual(State a, State b) {
        return a.getDistanceInWalk() <= b.getDistanceInWalk();
    }
}
