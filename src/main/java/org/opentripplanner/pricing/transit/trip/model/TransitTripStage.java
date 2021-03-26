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

    //Next minute after arriving at currentStop (1 for the first stage of the first fare in a trip)
    int time;

    //Distance between previously visited stop of the same fare and the current stop (0 for the first stop of each fare)
    double distance;

    public String toString() {
        return "{" + Optional.of(currentRoute.getShortName()).orElseGet(currentRoute::getLongName) + ", "
                + currentStop.getName() + ", " + time + " min, " + distance + " m}";

    }
}
