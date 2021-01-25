package org.opentripplanner.pricing;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;
import org.opentripplanner.model.Route;
import org.opentripplanner.pricing.ticket.pattern.RoutePattern;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@RequiredArgsConstructor
public class TransitTicketType {

    private enum ConstraintCategory {TIME, ROUTE_PATTERN, MAX_FARES}

    @Getter
    private final int id;

    //TODO: encapsulate default value when maxMinutes are not used in order to prevent errors?
    @Getter
    private final int maxMinutes;

    @Getter
    private final BigDecimal standardPrice;

    @Getter
    private final RoutePattern routePattern;

    @Getter
    @Setter
    private int maxFares = -1;

    public int getTotalMinutesWhenValid(int finishesAtMinute, List<Route> sortedRoutes, List<Pair<Integer, Integer>> routeTimeSpans) {
        HashMap<ConstraintCategory, Integer> totalMinutesWhenValid = new HashMap<>();
        totalMinutesWhenValid.put(ConstraintCategory.TIME, maxMinutes > -1 ? maxMinutes : finishesAtMinute);
        totalMinutesWhenValid.put(ConstraintCategory.ROUTE_PATTERN, finishesAtMinute);
        totalMinutesWhenValid.put(ConstraintCategory.MAX_FARES, finishesAtMinute);
        //TODO: include stopPattern compliant max time
        //TODO: include maxDistance compliant max time
        //TODO: rozbić na osobne metody sprawdzania warunków?

        //TODO: sortowanie!!
        Route currentRoute;
        int totalFareCount = 0;
        for (int i = sortedRoutes.size() - 1; i >= 0; i--) {
            currentRoute = sortedRoutes.get(i);
            int currentRouteStartMinute = routeTimeSpans.get(i).getLeft();
            if (!(currentRouteStartMinute > finishesAtMinute)) {
                if (totalFareCount < maxFares) {
                    totalFareCount++;
                    totalMinutesWhenValid.put(ConstraintCategory.MAX_FARES, finishesAtMinute - currentRouteStartMinute + 1);
                    //TODO: include fare switching rules
                }
                if (routePattern != null && routePattern.matches(currentRoute)) {
                    totalMinutesWhenValid.put(ConstraintCategory.ROUTE_PATTERN, finishesAtMinute - currentRouteStartMinute + 1);
                }
            }
        }

        return Collections.min(totalMinutesWhenValid.values());
    }

}
