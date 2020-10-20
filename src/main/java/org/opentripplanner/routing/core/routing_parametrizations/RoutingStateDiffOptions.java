package org.opentripplanner.routing.core.routing_parametrizations;

import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleType;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class RoutingStateDiffOptions implements Serializable {
    public boolean differRangeGroups = true;
    private List<Double> kickscooterRangeGroupsInMeters = Arrays.asList(20000D, 5000D, 1000D);
    private int numberOfKickScooterGroups = 4;

    public int getRangeGroup(State state) {
        if (state.getCurrentVehicle().getVehicleType() == VehicleType.KICKSCOOTER) {
            for (int i = 0; i < numberOfKickScooterGroups; i++) {
                if (state.vehicleHasEnoughRange(kickscooterRangeGroupsInMeters.get(i))) {
                    return i;
                }
            }
            return numberOfKickScooterGroups;
        }
        return 0;
    }

    public void setKickscooterRangeGroupsInMeters(List<Double> kickscooterRangeGroupsInMeters) {
        this.kickscooterRangeGroupsInMeters = kickscooterRangeGroupsInMeters;
        numberOfKickScooterGroups = kickscooterRangeGroupsInMeters.size();
    }
}

