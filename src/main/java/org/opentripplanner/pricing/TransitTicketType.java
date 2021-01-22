package org.opentripplanner.pricing;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.opentripplanner.model.Route;
import org.opentripplanner.pricing.ticket.pattern.RoutePattern;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
public class TransitTicketType {

    @Getter
    private int id;

    @Getter
    private int maxMinutes;

    @Getter
    private BigDecimal standardPrice;

    @Getter
    private RoutePattern routePattern;

    public int getTotalMinutesWhenValid(int finishesAtMinute, List<Route> sortedRoutes, List<Pair<Integer, Integer>> routeTimeSpans) {
        int totalMinutesWhenValid = finishesAtMinute;
        //TODO: include stopPattern compliant max time
        //TODO: include maxDistance compliant max time
        //TODO: include maxFares compliant max time

        //TODO: sortowanie!!
        if (routePattern != null) {
            Route currentRoute;
            for (int i = sortedRoutes.size() - 1; i >= 0; i--) {
                currentRoute = sortedRoutes.get(i);
                int currentRouteStartMinute = routeTimeSpans.get(i).getLeft();
                if (!(currentRouteStartMinute > finishesAtMinute)) {
                    if (routePattern.matches(currentRoute)) {
                        totalMinutesWhenValid = finishesAtMinute - currentRouteStartMinute + 1;
                    } else {
                        break;
                    }
                }
            }
        }

        return totalMinutesWhenValid; //TODO: return the result of this computations or maxMinutes/maxDistance-associated time (if some limit = -1, return the length of entire trip length
    }

}
