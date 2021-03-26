package org.opentripplanner.pricing.transit.ticket.pattern;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.opentripplanner.graph_builder.module.transit.tickets.parser.ConstraintsParser;
import org.opentripplanner.pricing.transit.trip.model.FareSwitch;

import java.util.ArrayList;
import java.util.Objects;

import static java.util.Objects.isNull;

/* The class is designed for scenarios like AT fare switch system (1), where (when using single-fare ticket)
 * it is only allowed to switch between predefined routes at predefined stops.
 *
 * 1. http://www.zdmikp.bydgoszcz.pl/pl/transport/bilety-i-oplaty/system-przesiadkowy-at),
 */

@JsonDeserialize(builder = FareSwitchPattern.FareSwitchPatternBuilder.class)
@RequiredArgsConstructor
public class FareSwitchPattern extends Pattern<FareSwitch> {

    //Constraints for route which we are alighting when switching fare
    @Getter
    private final RoutePattern previousRoutePattern;

    //Constraints for route which we are boarding when switching fare
    @Getter
    private final RoutePattern futureRoutePattern;

    //Constraints for the stop at which we are alighting previous route when switching fare
    @Getter
    private final StopPattern previousStopPattern;

    //Constraints for the stop at which we are boarding future route when switching fare
    @Getter
    private final StopPattern futureStopPattern;

    /* Is it allowed to switch fares in the opposite direction (where constraints for future
     * stop and future route are applied to previous route and previous stop)
     */
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

    public static final class FareSwitchPatternBuilder {

        @JsonProperty("previous_fare")
        private ArrayList<String> previousFareConstraints = null;

        @JsonProperty("future_fare")
        private ArrayList<String> futureFareConstraints = null;

        @JsonProperty("reverse_allowed")
        private boolean isReverseAllowed = false;

        public FareSwitchPattern build() {
            FareSwitchPattern builtPattern = new FareSwitchPattern(new RoutePattern(), new RoutePattern(),
                    new StopPattern(), new StopPattern(), isReverseAllowed);

            if (Objects.nonNull(previousFareConstraints)) {
                ConstraintsParser.parseConstraints(builtPattern.getPreviousRoutePattern(),
                        builtPattern.getPreviousStopPattern(),
                        previousFareConstraints);
            }

            if (Objects.nonNull(futureFareConstraints)) {
                ConstraintsParser.parseConstraints(builtPattern.getFutureRoutePattern(),
                        builtPattern.getFutureStopPattern(),
                        futureFareConstraints);
            }

            return builtPattern;
        }

    }

}
