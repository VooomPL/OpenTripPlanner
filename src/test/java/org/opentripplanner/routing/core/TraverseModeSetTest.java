package org.opentripplanner.routing.core;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TraverseModeSetTest {

    @Test
    public void testCarMode() {
        TraverseModeSet modeSet = new TraverseModeSet(TraverseMode.CAR);

        assertTrue(modeSet.getCar());
        assertFalse(modeSet.isTransit());
        assertFalse(modeSet.getRail());
        assertFalse(modeSet.getTram());
        assertFalse(modeSet.getSubway());
        assertFalse(modeSet.getFunicular());
        assertFalse(modeSet.getGondola());
        assertFalse(modeSet.getWalk());
        assertFalse(modeSet.getBicycle());
    }

    @Test
    public void testWalkMode() {
        TraverseModeSet modeSet = new TraverseModeSet(TraverseMode.WALK);

        assertTrue(modeSet.getWalk());
        assertFalse(modeSet.getCar());
        assertFalse(modeSet.isTransit());
        assertFalse(modeSet.getRail());
        assertFalse(modeSet.getTram());
        assertFalse(modeSet.getSubway());
        assertFalse(modeSet.getFunicular());
        assertFalse(modeSet.getGondola());
        assertFalse(modeSet.getBicycle());
    }

    @Test
    public void testBikeMode() {
        TraverseModeSet modeSet = new TraverseModeSet(TraverseMode.BICYCLE);

        assertTrue(modeSet.getBicycle());
        assertFalse(modeSet.getWalk());
        assertFalse(modeSet.getCar());
        assertFalse(modeSet.isTransit());
        assertFalse(modeSet.getRail());
        assertFalse(modeSet.getTram());
        assertFalse(modeSet.getSubway());
        assertFalse(modeSet.getFunicular());
        assertFalse(modeSet.getGondola());
        assertFalse(modeSet.getWalk());
    }

}
