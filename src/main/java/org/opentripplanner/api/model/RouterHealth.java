package org.opentripplanner.api.model;

import lombok.Data;

@Data
public class RouterHealth {

    private boolean configReady;
    private boolean vehiclePosition;
    // not vial for health
    private boolean traffic;
    // not vial for health
    private boolean vehiclePresence;

    boolean calculateHealth() {
        boolean health = configReady;
        if (System.getProperty("sharedVehiclesApi") != null) {
            health &= vehiclePosition;
        }
        return health;
    }
}
