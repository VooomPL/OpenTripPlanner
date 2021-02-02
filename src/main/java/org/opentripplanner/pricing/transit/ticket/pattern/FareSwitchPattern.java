package org.opentripplanner.pricing.transit.ticket.pattern;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.opentripplanner.pricing.transit.trip.model.FareSwitch;

import static java.util.Objects.nonNull;

@RequiredArgsConstructor
public class FareSwitchPattern extends Pattern<FareSwitch> {

    @Getter
    private final RoutePattern previousRoutePattern;

    @Getter
    private final RoutePattern futureRoutePattern;

    @Getter
    private final StopPattern previousStopPattern;

    @Getter
    private final StopPattern futureStopPattern;

    @Getter
    private final boolean isReverseAllowed;

    @Override
    public boolean matches(FareSwitch validatedObject) {
        boolean matches, matchesReversed = false;

        matches = (nonNull(previousRoutePattern) ? previousRoutePattern.matches(validatedObject.getPreviousRoute()) : true) &&
                (nonNull(previousStopPattern) ? previousStopPattern.matches(validatedObject.getPreviousStop()) : true) &&
                (nonNull(futureRoutePattern) ? futureRoutePattern.matches(validatedObject.getFutureRoute()) : true) &&
                (nonNull(futureStopPattern) ? futureStopPattern.matches(validatedObject.getFutureStop()) : true);

        if (!matches && isReverseAllowed) {
            matchesReversed = (nonNull(previousRoutePattern) ? previousRoutePattern.matches(validatedObject.getFutureRoute()) : true) &&
                    (nonNull(previousStopPattern) ? previousStopPattern.matches(validatedObject.getFutureStop()) : true) &&
                    (nonNull(futureRoutePattern) ? futureRoutePattern.matches(validatedObject.getPreviousRoute()) : true) &&
                    (nonNull(futureStopPattern) ? futureStopPattern.matches(validatedObject.getPreviousStop()) : true);
        }

        return matches || matchesReversed;
    }

}
