package org.opentripplanner.routing.services;

import org.opentripplanner.model.FeedScopedId;
import org.opentripplanner.routing.alertpatch.AlertPatch;

import java.util.Collection;
import java.util.Set;

public interface AlertPatchService {
    Collection<AlertPatch> getAllAlertPatches();

    Collection<AlertPatch> getStopPatches(FeedScopedId stop);

    Collection<AlertPatch> getRoutePatches(FeedScopedId route);

    void apply(AlertPatch alertPatch);

    void expire(Set<String> ids);

    void expireAll();

    void expireAllExcept(Set<String> ids);
}
