package org.opentripplanner.pricing.transit.trip.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.opentripplanner.model.Route;
import org.opentripplanner.model.Stop;

@AllArgsConstructor
public class TransitTripStage {

    @Getter
    private final Route currentRoute; //TODO: ensure non null?

    @Getter
    private final Stop currentStop;

    @Getter
    private final int time;

    @Getter
    private final int distance;
}
