package org.opentripplanner.pricing.ticket.pattern;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.opentripplanner.model.FeedScopedId;
import org.opentripplanner.model.Route;
import org.opentripplanner.pricing.TransitTicketType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TransitTicketTypeTest {

    @Test
    public void shouldReturn45MinutesValid() {
        Route firstRoute = new Route();
        firstRoute.setId(new FeedScopedId());
        firstRoute.setShortName("105");
        Route secondRoute = new Route();
        secondRoute.setId(new FeedScopedId());
        secondRoute.setShortName("13");

        List<Route> sortedRoutes = new ArrayList<>();
        List<Pair<Integer, Integer>> routeTimeSpans = new ArrayList<>();

        /*

        Routes used in the tested itinerary:

           10 min   5 min           35 min
        |----------|-----|---------------------------|
           105      walk             13

         */
        sortedRoutes.add(firstRoute);
        routeTimeSpans.add(new ImmutablePair<>(1, 10));
        sortedRoutes.add(secondRoute);
        routeTimeSpans.add(new ImmutablePair<>(16, 50));

        TransitTicketType ticketWithRouteConstraints = new TransitTicketType(4, -1, BigDecimal.valueOf(15), new RoutePattern());
        ticketWithRouteConstraints.getRoutePattern().addConstraint(RoutePattern.RouteAttribute.SHORT_NAME, Pattern.TextOperator.NOT_STARTS_WITH, "N");

        assertEquals(45, ticketWithRouteConstraints.getTotalMinutesWhenValid(45, sortedRoutes, routeTimeSpans));
    }

    @Test
    public void shouldReturn30MinutesValid() {
        Route firstRoute = new Route();
        firstRoute.setId(new FeedScopedId());
        firstRoute.setShortName("N30");
        Route secondRoute = new Route();
        secondRoute.setId(new FeedScopedId());
        secondRoute.setShortName("13");

        List<Route> sortedRoutes = new ArrayList<>();
        List<Pair<Integer, Integer>> routeTimeSpans = new ArrayList<>();

        /*

        Routes used in the tested itinerary:

           10 min   5 min           35 min
        |----------|-----|---------------------------|
           N30      walk             13

         */
        sortedRoutes.add(firstRoute);
        routeTimeSpans.add(new ImmutablePair<>(1, 10));
        sortedRoutes.add(secondRoute);
        routeTimeSpans.add(new ImmutablePair<>(16, 50));

        TransitTicketType ticketWithRouteConstraints = new TransitTicketType(4, -1, BigDecimal.valueOf(15), new RoutePattern());
        ticketWithRouteConstraints.getRoutePattern().addConstraint(RoutePattern.RouteAttribute.SHORT_NAME, Pattern.TextOperator.NOT_STARTS_WITH, "N");

        assertEquals(30, ticketWithRouteConstraints.getTotalMinutesWhenValid(45, sortedRoutes, routeTimeSpans));
    }

    @Test
    public void shouldReturnTotalRemainingTripTimeWhenRoutePatternIsNullDueToNoRouteConstraints() {
        Route firstRoute = new Route();
        firstRoute.setId(new FeedScopedId());
        firstRoute.setShortName("105");
        Route secondRoute = new Route();
        secondRoute.setId(new FeedScopedId());
        secondRoute.setShortName("13");

        List<Route> sortedRoutes = new ArrayList<>();
        List<Pair<Integer, Integer>> routeTimeSpans = new ArrayList<>();

        /*

        Routes used in the tested itinerary:

           10 min   5 min           35 min
        |----------|-----|---------------------------|
           105      walk             13

         */
        sortedRoutes.add(firstRoute);
        routeTimeSpans.add(new ImmutablePair<>(1, 10));
        sortedRoutes.add(secondRoute);
        routeTimeSpans.add(new ImmutablePair<>(16, 50));

        TransitTicketType ticketWithNullRouteConstraints = new TransitTicketType(4, -1, BigDecimal.valueOf(15), null);

        assertEquals(45, ticketWithNullRouteConstraints.getTotalMinutesWhenValid(45, sortedRoutes, routeTimeSpans));
    }

}
