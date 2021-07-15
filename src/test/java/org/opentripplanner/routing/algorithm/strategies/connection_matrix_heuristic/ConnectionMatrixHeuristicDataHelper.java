package org.opentripplanner.routing.algorithm.strategies.connection_matrix_heuristic;

import org.opentripplanner.graph_builder.module.connection_matrix_heuristic.ConnectionMatrixHeuristicDirectionData;
import org.opentripplanner.graph_builder.module.connection_matrix_heuristic.SerializedConnectionMatrixHeuristicData;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class ConnectionMatrixHeuristicDataHelper {

    public static ConnectionMatrixHeuristicData createData() {
        SerializedConnectionMatrixHeuristicData serializedData = new SerializedConnectionMatrixHeuristicData();
        serializedData.setLatMin(50.0);
        serializedData.setLatMax(50.003);
        serializedData.setLonMin(20.0001);
        serializedData.setLonMax(20.00015);
        serializedData.setHeight(3);
        serializedData.setWidth(5);
        serializedData.setInitialWeight(100.f);
        serializedData.setMaxSpeed(10000000);
        List<List<Float>> matrix = List.of(
                List.of(0.f, 1.f, 2.f, 3.f, 4.f),
                List.of(5.f, 6.f, 7.f, 8.f, 9.f),
                List.of(10.f, 11.f, 12.f, 13.f, 14.f)
        );
        serializedData.setDirectionData(Arrays.stream(Direction.values())
                .map(direction -> {
                    ConnectionMatrixHeuristicDirectionData data = new ConnectionMatrixHeuristicDirectionData();
                    data.setDirection(direction);
                    data.setData(matrix);
                    return data;
                })
                .collect(toList()));
        return new ConnectionMatrixHeuristicData(serializedData);
    }
}
