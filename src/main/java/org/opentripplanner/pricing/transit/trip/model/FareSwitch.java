package org.opentripplanner.pricing.transit.trip.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.opentripplanner.model.Route;
import org.opentripplanner.model.Stop;

@AllArgsConstructor
public class FareSwitch {

    @Getter
    private final Route previousRoute, futureRoute;

    @Getter
    private final Stop previousStop, futureStop;
}
