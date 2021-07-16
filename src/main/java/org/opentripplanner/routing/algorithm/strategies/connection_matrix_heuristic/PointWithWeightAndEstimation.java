package org.opentripplanner.routing.algorithm.strategies.connection_matrix_heuristic;

import lombok.Value;

@Value
class PointWithWeightAndEstimation implements Comparable<PointWithWeightAndEstimation> {

    Point point;

    float weight, estimation;

    @Override
    public int compareTo(PointWithWeightAndEstimation o) {
        return Float.compare(estimation, o.estimation);
    }
}
