package org.opentripplanner.routing.core.routing_parametrizations;

import static java.lang.Math.max;

public class RoutingPenalties {
    // TODO equals and hash code

    /**
     * This prevents unnecessary transfers by adding a cost for boarding a vehicle.
     */
    private int walkBoardCost = 60 * 10;

    public int getWalkBoardCost() {
        return walkBoardCost;
    }

    public void setWalkBoardCost(int walkBoardCost) {
        this.walkBoardCost = max(walkBoardCost, 0);
    }
}
