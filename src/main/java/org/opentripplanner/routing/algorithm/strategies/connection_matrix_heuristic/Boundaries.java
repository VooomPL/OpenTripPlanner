package org.opentripplanner.routing.algorithm.strategies.connection_matrix_heuristic;

import lombok.Getter;
import org.opentripplanner.graph_builder.module.connection_matrix_heuristic.SerializedConnectionMatrixHeuristicData;

import java.io.Serializable;

class Boundaries implements Serializable {

    private final double latMin, latMax, lonMin, lonMax;

    @Getter
    private final int width, height;

    private final double cellHeight, cellWidth;

    Boundaries(double latMin, double latMax, double lonMin, double lonMax, int height, int width) {
        this.latMin = latMin;
        this.latMax = latMax;
        this.height = height;
        cellHeight = (latMax - latMin) / height;

        this.lonMin = lonMin;
        this.lonMax = lonMax;
        this.width = width;
        cellWidth = (lonMax - lonMin) / width;
    }

    static Boundaries from(SerializedConnectionMatrixHeuristicData data) {
        return new Boundaries(data.getLatMin(), data.getLatMax(), data.getLonMin(), data.getLonMax(), data.getHeight(),
                data.getWidth());
    }

    Point createPointFrom(double lat, double lon) {
        int i = (int) ((lat - latMin) / cellHeight);
        int j = (int) ((lon - lonMin) / cellWidth);
        return new Point(height - i - 1, j);
    }

    boolean contains(Point point) {
        return point.getI() >= 0 && point.getI() < height && point.getJ() >= 0 && point.getJ() < width;
    }
}
