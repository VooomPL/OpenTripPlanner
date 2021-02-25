package org.opentripplanner.routing.algorithm.strategies;

import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.State;

/**
 * Estimate remaining weight by multiplying euclidean distance to destination by lowest multiplier
 * for all possible traverse modes. Multiplier is just reluctance divided by speed.
 */
public class SimpleLandmarkRWH extends SimpleEuclideanRWH {

    @Override
    public void initialize(RoutingRequest options, long abortTime) {
        super.initialize(options, abortTime);
    }

    @Override
    public double estimateRemainingWeight(State s) {
        return bestMultiplier * graph.getLandmarkEstimator().estimateDistanceInMeters(graph, s.getVertex(), destination);
    }


}
