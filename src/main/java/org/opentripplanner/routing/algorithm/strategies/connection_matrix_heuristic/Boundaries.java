package org.opentripplanner.routing.algorithm.strategies.connection_matrix_heuristic;

import lombok.Getter;
import org.opentripplanner.graph_builder.module.connection_matrix_heuristic.SerializedConnectionMatrixHeuristicData;

import java.io.Serializable;

import static org.opentripplanner.common.geometry.SphericalDistanceLibrary.fastDistance;

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
        int latIndex = (int) ((lat - latMin) / cellHeight);
        int lonIndex = (int) ((lon - lonMin) / cellWidth);
        return new Point(height - latIndex - 1, lonIndex);
    }

    boolean contains(Point point) {
        return point.getLatIndex() >= 0 && point.getLatIndex() < height && point.getLonIndex() >= 0 && point.getLonIndex() < width;
    }

    float distance(Point from, double toLat, double toLon) {
        double fromLat = latMin + (height - from.getLatIndex() - 1) * cellHeight + cellHeight / 2;
        double fromLon = lonMin + from.getLonIndex() * cellWidth + cellWidth / 2;
        return (float) fastDistance(fromLat, fromLon, toLat, toLon);
    }
}
