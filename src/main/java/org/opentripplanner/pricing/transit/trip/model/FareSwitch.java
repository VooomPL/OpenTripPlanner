package org.opentripplanner.pricing.transit.trip.model;

import lombok.Value;
import org.opentripplanner.model.Route;
import org.opentripplanner.model.Stop;

@Value
public class FareSwitch {

    Route previousRoute, futureRoute;
    Stop previousStop, futureStop;
}
