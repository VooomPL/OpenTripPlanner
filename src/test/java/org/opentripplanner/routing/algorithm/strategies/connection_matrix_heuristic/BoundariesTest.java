package org.opentripplanner.routing.algorithm.strategies.connection_matrix_heuristic;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BoundariesTest {

    @Test
    public void shouldCalculateIfPointIsInsideBoundaries() {
        // given
        Boundaries boundaries = new Boundaries(0.0, 5.0, 10.0, 13.0, 5, 3);
        Point inside1 = new Point(4, 2);
        Point inside2 = new Point(0, 0);
        Point outside1 = new Point(-1, 2);
        Point outside2 = new Point(4, 3);

        // then
        assertTrue(boundaries.contains(inside1));
        assertTrue(boundaries.contains(inside2));
        assertFalse(boundaries.contains(outside1));
        assertFalse(boundaries.contains(outside2));
    }

    @Test
    public void shouldCreatePointsFromCoordinates() {
        // given
        Boundaries boundaries = new Boundaries(0.0, 5.0, 10.0, 13.0, 5, 3);

        // then
        assertEquals(new Point(4, 0), boundaries.createPointFrom(0.5, 10.5));
        assertEquals(new Point(4, 2), boundaries.createPointFrom(0.5, 12.5));
        assertEquals(new Point(0, 0), boundaries.createPointFrom(4.5, 10.5));
        assertEquals(new Point(3, 1), boundaries.createPointFrom(1.5, 11.5));
    }
}