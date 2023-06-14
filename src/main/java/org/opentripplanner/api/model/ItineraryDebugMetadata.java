package org.opentripplanner.api.model;

import org.opentripplanner.routing.spt.GraphPath;

public class ItineraryDebugMetadata {

    public int visitedStates;

    public int generatedStates;

    public int enqueuedStates;

    public ItineraryDebugMetadata(GraphPath path) {
        this.visitedStates = path.getDebugMetadata().visitedStates;
        this.generatedStates = path.getDebugMetadata().generatedStates;
        this.enqueuedStates = path.getDebugMetadata().enqueuedStates;
    }
}
