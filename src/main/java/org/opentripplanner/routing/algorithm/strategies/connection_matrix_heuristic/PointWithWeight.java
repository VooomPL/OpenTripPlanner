package org.opentripplanner.routing.algorithm.strategies.connection_matrix_heuristic;

import lombok.Value;

@Value
class PointWithWeight implements Comparable<PointWithWeight> {

    Point point;

    float weight;

    @Override
    public int compareTo(PointWithWeight o) {
        return Float.compare(weight, o.weight);
    }
}
