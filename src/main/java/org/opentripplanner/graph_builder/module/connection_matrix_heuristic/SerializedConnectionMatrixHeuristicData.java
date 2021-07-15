package org.opentripplanner.graph_builder.module.connection_matrix_heuristic;

import lombok.Data;

import java.util.List;

@Data
public class SerializedConnectionMatrixHeuristicData {

    private List<ConnectionMatrixHeuristicDirectionData> directionData;

    private double latMin, latMax, lonMin, lonMax;

    private int width, height;
}
