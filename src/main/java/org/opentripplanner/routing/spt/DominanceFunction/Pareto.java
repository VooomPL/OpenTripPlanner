package org.opentripplanner.routing.spt.DominanceFunction;

import org.opentripplanner.routing.core.State;

/**
 * In this implementation the relation is not symmetric. There are sets of mutually co-dominant states.
 */
public class Pareto extends DominanceFunction {

    @Override
    public boolean betterOrEqual(State a, State b) {

        // The key problem in pareto-dominance in OTP is that the elements of the state vector are not orthogonal.
        // When walk distance increases, weight increases. When time increases weight increases.
        // It's easy to get big groups of very similar states that don't represent significantly different outcomes.
        // Our solution to this is to give existing states some slack to dominate new states more easily.

        final double EPSILON = 1e-4;
        return (a.getElapsedTimeSeconds() <= (b.getElapsedTimeSeconds() + EPSILON)
                && a.getWeight() <= (b.getWeight() + EPSILON));

    }
}
