package org.opentripplanner.graph_builder.module.connection_matrix_heuristic;

import lombok.Data;
import org.opentripplanner.routing.algorithm.strategies.connection_matrix_heuristic.Direction;

import java.util.List;

@Data
public class ConnectionMatrixHeuristicDirectionData {

    private Direction direction;

    private List<List<Float>> data;

    public Float[][] getDataAsArray() {
        return data.stream()
                .map(l -> l.toArray(Float[]::new))
                .toArray(Float[][]::new);
    }
}
