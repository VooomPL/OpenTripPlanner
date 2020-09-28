package org.opentripplanner.routing.spt.DominanceFunction;

public class DominanceFunctionSettings {
    private boolean differProvider = false;
    private boolean differRange = false;
    private boolean differEnoughRange = false;

    public boolean isDifferMayLeaveAtDestination() {
        return differMayLeaveAtDestination;
    }

    public void setDifferMayLeaveAtDestination(boolean differMayLeaveAtDestination) {
        this.differMayLeaveAtDestination = differMayLeaveAtDestination;
    }

    private boolean differMayLeaveAtDestination = false;

    public boolean isDifferProvider() {
        return differProvider;
    }

    public void setDifferProvider(boolean differProvider) {
        this.differProvider = differProvider;
    }

    public boolean isDifferRange() {
        return differRange;
    }

    public void setDifferRange(boolean differRange) {
        this.differRange = differRange;
    }

    public boolean isDifferEnoughRange() {
        return differEnoughRange;
    }

    public void setDifferEnoughRange(boolean differEnoughRange) {
        this.differEnoughRange = differEnoughRange;
    }
}
