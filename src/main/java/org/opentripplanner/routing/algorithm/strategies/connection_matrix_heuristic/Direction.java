package org.opentripplanner.routing.algorithm.strategies.connection_matrix_heuristic;

public enum Direction {

    NW,
    N,
    NE,
    E,
    SE,
    S,
    SW,
    W;

    private boolean isNorth() {
        return this == NW || this == N || this == NE;
    }

    private boolean isSouth() {
        return this == SW || this == S || this == SE;
    }

    private boolean isWest() {
        return this == NW || this == W || this == SW;
    }

    private boolean isEast() {
        return this == NE || this == E || this == SE;
    }

    private int getSNOffset() {
        if (isSouth()) {
            return -1;
        } else if (isNorth()) {
            return 1;
        } else {
            return 0;
        }
    }

    private int getEWOffset() {
        if (isWest()) {
            return -1;
        } else if (isEast()) {
            return 1;
        } else {
            return 0;
        }
    }

    Point neighbor(Point point) {
        return new Point(point.getI() + getSNOffset(), point.getJ() + getEWOffset());
    }
}
