package org.opentripplanner.pricing.transit.trip.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.opentripplanner.model.Route;
import org.opentripplanner.model.Stop;

import java.util.Optional;

@AllArgsConstructor
public class TransitTripStage {

    @Getter
    private final Route currentRoute;

    @Getter
    private final Stop currentStop;

    @Getter
    private final int time;

    @Getter
    private final double distance;

    public String toString() {
        return Optional.of("{" + currentRoute.getShortName()).orElseGet(currentRoute::getLongName) + ", "
                + currentStop.getName() + ", " + time + " min, " + distance + " m}";

    }
}
