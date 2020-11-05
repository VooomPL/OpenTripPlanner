package org.opentripplanner.routing.spt;

import org.opentripplanner.routing.core.State;

@FunctionalInterface
public interface StateFeature {
    boolean doesStateHaveFeature(State state);
}
