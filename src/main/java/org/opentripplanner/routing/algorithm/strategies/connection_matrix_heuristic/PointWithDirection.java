package org.opentripplanner.routing.algorithm.strategies.connection_matrix_heuristic;

import lombok.Value;

@Value
class PointWithDirection {

    Point point;

    Direction direction;
}
