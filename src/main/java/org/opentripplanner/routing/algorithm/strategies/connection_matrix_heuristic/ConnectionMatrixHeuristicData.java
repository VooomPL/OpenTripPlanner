package org.opentripplanner.routing.algorithm.strategies.connection_matrix_heuristic;

import lombok.AccessLevel;
import lombok.Getter;
import org.opentripplanner.common.model.GenericLocation;
import org.opentripplanner.graph_builder.module.connection_matrix_heuristic.ConnectionMatrixHeuristicDirectionData;
import org.opentripplanner.graph_builder.module.connection_matrix_heuristic.SerializedConnectionMatrixHeuristicData;
import org.opentripplanner.routing.graph.Vertex;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public class ConnectionMatrixHeuristicData implements Serializable {

    private final Map<Direction, Float[][]> data;

    @Getter(AccessLevel.PACKAGE)
    private final Boundaries boundaries;

    public ConnectionMatrixHeuristicData(SerializedConnectionMatrixHeuristicData data) {
        this.data = data.getDirectionData().stream().collect(toMap(
                ConnectionMatrixHeuristicDirectionData::getDirection,
                ConnectionMatrixHeuristicDirectionData::getDataAsArray));
        boundaries = Boundaries.from(data);
        int width = boundaries.getWidth();
        int height = boundaries.getHeight();
        for (Float[][] array : this.data.values()) {
            for (int i = 0; i < height; i++)
                for (int j = 0; j < width; j++)
                    if (array[i][j] == 0.)
                        array[i][j] = Float.NaN;
        }
    }

    Stream<PointWithDirection> getNeighbors(Point point) {
        return Arrays.stream(Direction.values())
                .map(direction -> new PointWithDirection(direction.neighbor(point), direction))
                .filter(p -> boundaries.contains(p.getPoint()));
    }

    Point mapToPoint(Vertex v) {
        return boundaries.createPointFrom(v.getLat(), v.getLon());
    }

    Point mapToPoint(GenericLocation location) {
        return boundaries.createPointFrom(location.lat, location.lng);
    }

    float getCost(Point from, Direction direction) {
        return data.get(direction)[from.getI()][from.getJ()];
    }
}
