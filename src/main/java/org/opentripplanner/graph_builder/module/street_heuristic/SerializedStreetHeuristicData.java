package org.opentripplanner.graph_builder.module.street_heuristic;

import lombok.Data;

import java.util.List;

@Data
public class SerializedStreetHeuristicData {

    private List<StreetHeuristicDirectionData> directionData;

    private double latMin, latMax, lonMin, lonMax;

    private int width, height;
}
