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

    @Test
    public void shouldReturn15MinutesValidDueToTimeRestrictions() {
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

        TransitTicketType ticketWithTimeConstraints = new TransitTicketType(4, 15, BigDecimal.valueOf(15), new RoutePattern());

        assertEquals(15, ticketWithTimeConstraints.getTotalMinutesWhenValid(45, sortedRoutes, routeTimeSpans));
    }

    @Test
    public void shouldReturn15MinutesValidRegardlessRouteConstraints() {
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

        TransitTicketType ticketWithTimeAndRouteConstraints = new TransitTicketType(4, 15, BigDecimal.valueOf(15), new RoutePattern());
        ticketWithTimeAndRouteConstraints.getRoutePattern().addConstraint(RoutePattern.RouteAttribute.SHORT_NAME, Pattern.TextOperator.NOT_STARTS_WITH, "N");

        assertEquals(15, ticketWithTimeAndRouteConstraints.getTotalMinutesWhenValid(45, sortedRoutes, routeTimeSpans));
    }

    @Test
    public void shouldReturn45MinutesValidDueToMaxFaresLimitOnly() {
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

        TransitTicketType ticketWithMaxFaresConstraints = new TransitTicketType(4, -1, BigDecimal.valueOf(15), null);
        ticketWithMaxFaresConstraints.setMaxFares(2);

        assertEquals(45, ticketWithMaxFaresConstraints.getTotalMinutesWhenValid(45, sortedRoutes, routeTimeSpans));
    }

    @Test
    public void shouldReturn30MinutesValidDueToMaxFaresLimitOnly() {
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

        TransitTicketType ticketWithMaxFaresConstraints = new TransitTicketType(4, -1, BigDecimal.valueOf(15), null);
        ticketWithMaxFaresConstraints.setMaxFares(1);

        assertEquals(30, ticketWithMaxFaresConstraints.getTotalMinutesWhenValid(45, sortedRoutes, routeTimeSpans));
    }

    @Test
    public void shouldReturn30MinutesValidDueToMaxFaresLimit() {
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

        TransitTicketType ticketWithRouteAndMaxFaresConstraints = new TransitTicketType(4, -1, BigDecimal.valueOf(15), new RoutePattern());
        ticketWithRouteAndMaxFaresConstraints.setMaxFares(1);
        ticketWithRouteAndMaxFaresConstraints.getRoutePattern().addConstraint(RoutePattern.RouteAttribute.SHORT_NAME, Pattern.TextOperator.NOT_STARTS_WITH, "N");

        assertEquals(30, ticketWithRouteAndMaxFaresConstraints.getTotalMinutesWhenValid(45, sortedRoutes, routeTimeSpans));
    }

    @Test
    public void shouldReturn30MinutesValidDueToRouteConstraints() {
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

        TransitTicketType ticketWithRouteAndMaxFaresConstraints = new TransitTicketType(4, -1, BigDecimal.valueOf(15), new RoutePattern());
        ticketWithRouteAndMaxFaresConstraints.getRoutePattern().addConstraint(RoutePattern.RouteAttribute.SHORT_NAME, Pattern.TextOperator.NOT_STARTS_WITH, "N");
        ticketWithRouteAndMaxFaresConstraints.setMaxFares(2);
        assertEquals(30, ticketWithRouteAndMaxFaresConstraints.getTotalMinutesWhenValid(45, sortedRoutes, routeTimeSpans));
    }

}
