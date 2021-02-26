package org.opentripplanner.pricing.transit.trip.model;

import lombok.Value;
import org.opentripplanner.model.Route;
import org.opentripplanner.model.Stop;

import java.util.Optional;

@Value
public class TransitTripStage {

    Route currentRoute;

    //Stop at which this stage begins
    Stop currentStop;

    int time;

    double distance;

    public String toString() {
        return "{" + Optional.of(currentRoute.getShortName()).orElseGet(currentRoute::getLongName) + ", "
                + currentStop.getName() + ", " + time + " min, " + distance + " m}";

    }
}
