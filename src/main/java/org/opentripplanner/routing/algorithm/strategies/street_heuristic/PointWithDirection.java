package org.opentripplanner.routing.algorithm.strategies.street_heuristic;

import lombok.Value;

@Value
class PointWithDirection {

    Point point;

    Direction direction;
}
