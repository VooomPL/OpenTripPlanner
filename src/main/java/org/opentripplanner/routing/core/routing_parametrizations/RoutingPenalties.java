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

    /**
     * An extra penalty added on transfers (i.e. all boardings except the first one).
     * Not to be confused with bikeBoardCost and walkBoardCost, which are the cost of boarding a
     * vehicle with and without a bicycle. The boardCosts are used to model the 'usual' perceived
     * cost of using a transit vehicle, and the transferPenalty is used when a user requests even
     * less transfers. In the latter case, we don't actually optimize for fewest transfers, as this
     * can lead to absurd results. Consider a trip in New York from Grand Army
     * Plaza (the one in Brooklyn) to Kalustyan's at noon. The true lowest transfers route is to
     * wait until midnight, when the 4 train runs local the whole way. The actual fastest route is
     * the 2/3 to the 4/5 at Nevins to the 6 at Union Square, which takes half an hour.
     * Even someone optimizing for fewest transfers doesn't want to wait until midnight. Maybe they
     * would be willing to walk to 7th Ave and take the Q to Union Square, then transfer to the 6.
     * If this takes less than optimize_transfer_penalty seconds, then that's what we'll return.
     */
    private int transferPenalty = 0;

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

    public int getTransferPenalty() {
        return transferPenalty;
    }

    public void setTransferPenalty(int transferPenalty) {
        this.transferPenalty = max(transferPenalty, 0);
    }
}
