package org.opentripplanner.routing.algorithm.strategies.connection_matrix_heuristic;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ConnectionMatrixHeuristicDataTest {

    private ConnectionMatrixHeuristicData data;

    @Before
    public void setUp() {
        data = ConnectionMatrixHeuristicDataHelper.createData();
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