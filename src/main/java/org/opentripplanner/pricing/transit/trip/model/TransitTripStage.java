package org.opentripplanner.pricing.transit.trip.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.opentripplanner.model.Route;
import org.opentripplanner.model.Stop;

@AllArgsConstructor
public class TransitTripStage {
    //TODO: ensure non null attributes?
    @Getter
    private final Route currentRoute;

    @Getter
    private final Stop currentStop;

    @Getter
    private final int time;

    @Getter
    private final int distance;
}
