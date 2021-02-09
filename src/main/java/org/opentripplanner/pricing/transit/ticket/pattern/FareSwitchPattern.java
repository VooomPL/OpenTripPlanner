package org.opentripplanner.pricing.transit.ticket.pattern;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.opentripplanner.pricing.transit.trip.model.FareSwitch;

import static java.util.Objects.isNull;

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

        matches = (isNull(previousRoutePattern) || previousRoutePattern.matches(validatedObject.getPreviousRoute())) &&
                (isNull(previousStopPattern) || previousStopPattern.matches(validatedObject.getPreviousStop())) &&
                (isNull(futureRoutePattern) || futureRoutePattern.matches(validatedObject.getFutureRoute())) &&
                (isNull(futureStopPattern) || futureStopPattern.matches(validatedObject.getFutureStop()));

        if (!matches && isReverseAllowed) {
            matchesReversed = (isNull(previousRoutePattern) || previousRoutePattern.matches(validatedObject.getFutureRoute())) &&
                    (isNull(previousStopPattern) || previousStopPattern.matches(validatedObject.getFutureStop())) &&
                    (isNull(futureRoutePattern) || futureRoutePattern.matches(validatedObject.getPreviousRoute())) &&
                    (isNull(futureStopPattern) || futureStopPattern.matches(validatedObject.getPreviousStop()));
        }

        return matches || matchesReversed;
    }

}
