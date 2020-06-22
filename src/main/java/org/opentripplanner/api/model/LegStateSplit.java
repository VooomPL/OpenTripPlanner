package org.opentripplanner.api.model;

import org.opentripplanner.routing.core.State;

import java.util.List;

public class LegStateSplit {

    List<State> states;
    List<State> legSwitchStates;

    public LegStateSplit(List<State> states, List<State> legSwitchStates) {
        this.states = states;
        this.legSwitchStates = legSwitchStates;
    }

    public List<State> getStates() {
        return states;
    }

    public List<State> getLegSwitchStates() {
        return legSwitchStates;
    }
}
