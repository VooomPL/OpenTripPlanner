package org.opentripplanner.api.model;

import lombok.Data;

@Data
public class RouterHealth {

    private boolean configReady;
    private boolean vehiclePosition;
    private boolean traffic;
    private boolean vehiclePresence;

    boolean calculateHealth() {
        boolean health = configReady;
        if (System.getProperty("sharedVehiclesApi") != null) {
            health &= vehiclePosition;
        }
        if (System.getProperty("trfficApi") != null) {
            health &= traffic;
        }
        if (System.getProperty("predictionApiUrl") != null) {
            health &= vehiclePosition;
        }
        return health;
    }
}
