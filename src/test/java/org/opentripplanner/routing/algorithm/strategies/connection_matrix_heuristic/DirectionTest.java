package org.opentripplanner.routing.algorithm.strategies.connection_matrix_heuristic;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DirectionTest {

    @Test
    public void shouldCalculateNeighborsCorrectly() {
        // given
        Point point = new Point(7, 3);

        // then
        //   2 3 4
        // 8 a b c
        // 7 d * e
        // 6 f g h
        assertEquals(new Point(8, 2), Direction.NW.neighbor(point)); // a
        assertEquals(new Point(8, 3), Direction.N.neighbor(point)); // b
        assertEquals(new Point(8, 4), Direction.NE.neighbor(point)); // c
        assertEquals(new Point(7, 2), Direction.W.neighbor(point)); // d
        assertEquals(new Point(7, 4), Direction.E.neighbor(point)); // e
        assertEquals(new Point(6, 2), Direction.SW.neighbor(point)); // f
        assertEquals(new Point(6, 3), Direction.S.neighbor(point)); // g
        assertEquals(new Point(6, 4), Direction.SE.neighbor(point)); // h
    }
}