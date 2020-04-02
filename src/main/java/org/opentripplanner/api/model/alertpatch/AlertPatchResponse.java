package org.opentripplanner.api.model.alertpatch;

import org.opentripplanner.routing.alertpatch.AlertPatch;

import java.util.ArrayList;
import java.util.List;

public class AlertPatchResponse {
    public List<AlertPatch> alertPatches;

    public void addAlertPatch(AlertPatch alertPatch) {
        if (alertPatches == null) {
            alertPatches = new ArrayList<AlertPatch>();
        }
        alertPatches.add(alertPatch);
    }
}
