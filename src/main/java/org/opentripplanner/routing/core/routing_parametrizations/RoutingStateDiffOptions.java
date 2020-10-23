package org.opentripplanner.routing.core.routing_parametrizations;

import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class RoutingStateDiffOptions implements Serializable {
    public boolean differRangeGroups = true;
    private ArrayList<Double> kickscooterRangeGroupsInMeters = new ArrayList<Double>(Arrays.asList(20000D, 5000D, 1000D));
    private int numberOfKickScooterGroups = 4;

    //Bigger range group means further range
    public int getRangeGroup(State state) {
        if (state.getCurrentVehicle().getVehicleType() == VehicleType.KICKSCOOTER) {
            for (int i = 0; i < numberOfKickScooterGroups - 1; i++) {
                if (!state.vehicleHasEnoughRange(kickscooterRangeGroupsInMeters.get(i))) {
                    return i;
                }
            }
            return numberOfKickScooterGroups - 1;
        }
        return 0;
    }

    public void setKickscooterRangeGroupsInMeters(ArrayList<Double> kickscooterRangeGroupsInMeters) {
        this.kickscooterRangeGroupsInMeters = kickscooterRangeGroupsInMeters;
        Collections.sort(this.kickscooterRangeGroupsInMeters);
        numberOfKickScooterGroups = kickscooterRangeGroupsInMeters.size() + 1;
    }
}

