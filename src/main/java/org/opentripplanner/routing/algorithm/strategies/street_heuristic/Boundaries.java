package org.opentripplanner.routing.algorithm.strategies.street_heuristic;

import lombok.Getter;
import org.opentripplanner.graph_builder.module.street_heuristic.SerializedStreetHeuristicData;

import java.io.Serializable;

class Boundaries implements Serializable {

    private final double latMin, latMax, lonMin, lonMax;

    @Getter
    private final int width, height;

    private final double cellHeight, cellWidth, latBegin, lonBegin, latEnd, lonEnd;

    Boundaries(double latMin, double latMax, double lonMin, double lonMax, int width, int height) {
        this.latMin = latMin;
        this.latMax = latMax;
        this.lonMin = lonMin;
        this.lonMax = lonMax;
        this.width = width;
        this.height = height;
        cellHeight = (latMax - latMin) / height;
        cellWidth = (lonMax - lonMin) / width;
        latBegin = latMin - cellHeight / 2;
        lonBegin = lonMin - cellWidth / 2;
        latEnd = latMax + cellHeight / 2;
        lonEnd = lonMax + cellWidth / 2;
    }

    static Boundaries from(SerializedStreetHeuristicData data) {
        return new Boundaries(data.getLatMin(), data.getLatMax(), data.getLonMin(), data.getLonMax(), data.getWidth(),
                data.getHeight());
    }

    Point createPointFrom(double lat, double lon) {
        int x = (int) ((lat - latBegin) / cellHeight);
        int y = (int) ((lon - lonBegin) / cellWidth);
        return new Point(x, y);
    }

    boolean contains(Point point) {
        return point.getX() >= latBegin && point.getX() <= latEnd && point.getY() >= lonBegin && point.getY() <= lonEnd;
    }
}
