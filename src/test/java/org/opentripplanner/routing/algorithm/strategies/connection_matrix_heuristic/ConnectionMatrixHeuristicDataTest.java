package org.opentripplanner.routing.algorithm.strategies.connection_matrix_heuristic;

import org.junit.Before;
import org.junit.Test;
import org.opentripplanner.graph_builder.module.connection_matrix_heuristic.ConnectionMatrixHeuristicDirectionData;
import org.opentripplanner.graph_builder.module.connection_matrix_heuristic.SerializedConnectionMatrixHeuristicData;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ConnectionMatrixHeuristicDataTest {

    private ConnectionMatrixHeuristicData data;

    @Before
    public void setUp() {
        SerializedConnectionMatrixHeuristicData serializedData = new SerializedConnectionMatrixHeuristicData();
        serializedData.setLatMin(0.0);
        serializedData.setLatMax(3.0);
        serializedData.setLonMin(10.0);
        serializedData.setLonMax(15.0);
        serializedData.setHeight(3);
        serializedData.setWidth(5);
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
        data = new ConnectionMatrixHeuristicData(serializedData);
    }

    @Test
    public void shouldReturnNeighbors() {
        // then
        assertEquals(3, data.getNeighbors(new Point(0, 0)).count());
        assertEquals(5, data.getNeighbors(new Point(0, 1)).count());
        assertEquals(5, data.getNeighbors(new Point(1, 0)).count());
        assertEquals(8, data.getNeighbors(new Point(1, 1)).count());
    }

    @Test
    public void shouldReturnProperCost() {
        // then
        assertTrue(Float.isNaN(data.getCost(new Point(0, 0), Direction.N)));
        assertEquals(14.f, data.getCost(new Point(2, 4), Direction.N), 0.0);
    }
}