package org.opentripplanner.routing.core.routing_parametrizations;

import org.opentripplanner.routing.core.TraverseMode;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class RoutingPenalties {
    // TODO equals and hash code

    /**
     * This prevents unnecessary transfers by adding a cost for boarding a vehicle.
     */
    private int walkBoardCost = 60 * 10;

    /**
     * Separate cost for boarding a vehicle with a bicycle, which is more difficult than on foot.
     */
    private int bikeBoardCost = 60 * 10;

    public int getWalkBoardCost() {
        return walkBoardCost;
    }

    public void setWalkBoardCost(int walkBoardCost) {
        this.walkBoardCost = max(walkBoardCost, 0);
    }

    public int getBikeBoardCost() {
        return bikeBoardCost;
    }

    public void setBikeBoardCost(int bikeBoardCost) {
        this.bikeBoardCost = max(bikeBoardCost, 0);
    }

    public int getBoardCost(TraverseMode mode) {
        // I assume you can't bring your car in the bus
        return mode == TraverseMode.BICYCLE ? bikeBoardCost : walkBoardCost;
    }

    public int getBoardCostLowerBound() {
        return min(walkBoardCost, bikeBoardCost);
    }
}
