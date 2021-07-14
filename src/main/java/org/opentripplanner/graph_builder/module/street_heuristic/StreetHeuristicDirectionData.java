package org.opentripplanner.graph_builder.module.street_heuristic;

import lombok.Data;
import org.opentripplanner.routing.algorithm.strategies.street_heuristic.Direction;

import java.util.List;

@Data
public class StreetHeuristicDirectionData {

    private Direction direction;
    private List<List<Float>> data;

    public Float[][] getDataAsArray() {
        return data.stream()
                .map(l -> l.toArray(Float[]::new))
                .toArray(Float[][]::new);
    }
}
