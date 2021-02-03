package org.opentripplanner.pricing.transit.ticket;

import org.junit.Test;
import org.opentripplanner.model.FeedScopedId;
import org.opentripplanner.model.Route;
import org.opentripplanner.model.Stop;
import org.opentripplanner.pricing.transit.ticket.pattern.FareSwitchPattern;
import org.opentripplanner.pricing.transit.ticket.pattern.Pattern;
import org.opentripplanner.pricing.transit.ticket.pattern.RoutePattern;
import org.opentripplanner.pricing.transit.ticket.pattern.StopPattern;
import org.opentripplanner.pricing.transit.trip.model.TransitTripStage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TransitTicketTest {

    @Test
    public void shouldReturn45MinutesValid() {
        Route firstRoute = new Route();
        firstRoute.setId(new FeedScopedId("ZTM", "105"));
        firstRoute.setShortName("105");
        Route secondRoute = new Route();
        secondRoute.setId(new FeedScopedId("ZTM", "13"));
        secondRoute.setShortName("13");

        Stop genericStop = new Stop();
        genericStop.setId(new FeedScopedId());

        List<TransitTripStage> tripStages = new ArrayList<>();

        /*

        Routes used in the tested itinerary:

           10 min   5 min           35 min
        |----------|-----|---------------------------|
             105     walk             13

         */
        tripStages.add(new TransitTripStage(firstRoute, genericStop, 1, 0));
        tripStages.add(new TransitTripStage(firstRoute, genericStop, 11, 0));
        tripStages.add(new TransitTripStage(secondRoute, genericStop, 16, 0));
        tripStages.add(new TransitTripStage(secondRoute, genericStop, 51, 0));

        TransitTicket ticketWithRouteConstraints = TransitTicket.builder(4, BigDecimal.valueOf(15)).build();
        ticketWithRouteConstraints.getRoutePattern().addConstraint(RoutePattern.RouteAttribute.SHORT_NAME,
                Pattern.TextOperator.NOT_STARTS_WITH, "N");

        assertEquals(45, ticketWithRouteConstraints.getTotalMinutesWhenValid(45, tripStages));
    }

    @Test
    public void shouldReturn30MinutesValid() {
        Route firstRoute = new Route();
        firstRoute.setId(new FeedScopedId("ZTM", "N30"));
        firstRoute.setShortName("N30");
        Route secondRoute = new Route();
        secondRoute.setId(new FeedScopedId("ZTM", "13"));
        secondRoute.setShortName("13");

        Stop genericStop = new Stop();
        genericStop.setId(new FeedScopedId());

        List<TransitTripStage> tripStages = new ArrayList<>();

        /*

        Routes used in the tested itinerary:

           10 min   5 min           35 min
        |----------|-----|---------------------------|
           N30      walk             13

         */
        tripStages.add(new TransitTripStage(firstRoute, genericStop, 1, 0));
        tripStages.add(new TransitTripStage(firstRoute, genericStop, 11, 0));
        tripStages.add(new TransitTripStage(secondRoute, genericStop, 16, 0));
        tripStages.add(new TransitTripStage(secondRoute, genericStop, 51, 0));

        TransitTicket ticketWithRouteConstraints = TransitTicket.builder(4, BigDecimal.valueOf(15)).build();
        ticketWithRouteConstraints.getRoutePattern().addConstraint(RoutePattern.RouteAttribute.SHORT_NAME,
                Pattern.TextOperator.NOT_STARTS_WITH, "N");

        assertEquals(30, ticketWithRouteConstraints.getTotalMinutesWhenValid(45, tripStages));
    }

    @Test
    public void shouldReturn30MinutesValidThoughFirstRouteIsConstraintCompliant() {
        Route firstRoute = new Route();
        firstRoute.setId(new FeedScopedId("ZTM", "512"));
        firstRoute.setShortName("512");
        Route secondRoute = new Route();
        secondRoute.setId(new FeedScopedId("ZTM", "N30"));
        secondRoute.setShortName("N30");
        Route thirdRoute = new Route();
        thirdRoute.setId(new FeedScopedId("ZTM", "13"));
        thirdRoute.setShortName("13");

        Stop genericStop = new Stop();
        genericStop.setId(new FeedScopedId());

        List<TransitTripStage> tripStages = new ArrayList<>();

        /*

        Routes used in the tested itinerary:

            10 min  5 min   10 min   5 min           35 min
        |----------|-----|----------|-----|---------------------------|
            512      walk    N30      walk             13

         */
        tripStages.add(new TransitTripStage(firstRoute, genericStop, 1, 0));
        tripStages.add(new TransitTripStage(firstRoute, genericStop, 11, 0));
        tripStages.add(new TransitTripStage(secondRoute, genericStop, 16, 0));
        tripStages.add(new TransitTripStage(secondRoute, genericStop, 26, 0));
        tripStages.add(new TransitTripStage(thirdRoute, genericStop, 31, 0));
        tripStages.add(new TransitTripStage(thirdRoute, genericStop, 66, 0));

        TransitTicket ticketWithRouteConstraints = TransitTicket.builder(4, BigDecimal.valueOf(15)).build();
        ticketWithRouteConstraints.getRoutePattern().addConstraint(RoutePattern.RouteAttribute.SHORT_NAME,
                Pattern.TextOperator.NOT_STARTS_WITH, "N");

        assertEquals(30, ticketWithRouteConstraints.getTotalMinutesWhenValid(60, tripStages));
    }

    @Test
    public void shouldReturnTotalRemainingTripTimeWhenRoutePatternIsNullDueToNoRouteConstraints() {
        Route firstRoute = new Route();
        firstRoute.setId(new FeedScopedId("ZTM", "105"));
        firstRoute.setShortName("105");
        Route secondRoute = new Route();
        secondRoute.setId(new FeedScopedId("ZTM", "13"));
        secondRoute.setShortName("13");

        Stop genericStop = new Stop();
        genericStop.setId(new FeedScopedId());

        List<TransitTripStage> tripStages = new ArrayList<>();

        /*

        Routes used in the tested itinerary:

           10 min   5 min           35 min
        |----------|-----|---------------------------|
           105      walk             13

         */
        tripStages.add(new TransitTripStage(firstRoute, genericStop, 1, 0));
        tripStages.add(new TransitTripStage(firstRoute, genericStop, 11, 0));
        tripStages.add(new TransitTripStage(secondRoute, genericStop, 16, 0));
        tripStages.add(new TransitTripStage(secondRoute, genericStop, 51, 0));

        TransitTicket ticketWithNullRouteConstraints = TransitTicket.builder(4, BigDecimal.valueOf(15)).build();

        assertEquals(45, ticketWithNullRouteConstraints.getTotalMinutesWhenValid(45, tripStages));
    }

    @Test
    public void shouldReturn15MinutesValidDueToTimeRestrictions() {
        Route firstRoute = new Route();
        firstRoute.setId(new FeedScopedId("ZTM", "N30"));
        firstRoute.setShortName("N30");
        Route secondRoute = new Route();
        secondRoute.setId(new FeedScopedId("ZTM", "13"));
        secondRoute.setShortName("13");

        Stop genericStop = new Stop();
        genericStop.setId(new FeedScopedId());

        List<TransitTripStage> tripStages = new ArrayList<>();

        /*

        Routes used in the tested itinerary:

           10 min   5 min           35 min
        |----------|-----|---------------------------|
           N30      walk             13

         */
        tripStages.add(new TransitTripStage(firstRoute, genericStop, 1, 0));
        tripStages.add(new TransitTripStage(firstRoute, genericStop, 11, 0));
        tripStages.add(new TransitTripStage(secondRoute, genericStop, 16, 0));
        tripStages.add(new TransitTripStage(secondRoute, genericStop, 51, 0));

        TransitTicket ticketWithTimeConstraints = TransitTicket.builder(4, BigDecimal.valueOf(15)).setTimeLimit(15).build();

        assertEquals(15, ticketWithTimeConstraints.getTotalMinutesWhenValid(45, tripStages));
    }

    @Test
    public void shouldReturn15MinutesValidRegardlessRouteConstraints() {
        Route firstRoute = new Route();
        firstRoute.setId(new FeedScopedId("ZTM", "N30"));
        firstRoute.setShortName("N30");
        Route secondRoute = new Route();
        secondRoute.setId(new FeedScopedId("ZTM", "13"));
        secondRoute.setShortName("13");

        Stop genericStop = new Stop();
        genericStop.setId(new FeedScopedId());

        List<TransitTripStage> tripStages = new ArrayList<>();

        /*

        Routes used in the tested itinerary:

           10 min   5 min           35 min
        |----------|-----|---------------------------|
           N30      walk             13

         */
        tripStages.add(new TransitTripStage(firstRoute, genericStop, 1, 0));
        tripStages.add(new TransitTripStage(firstRoute, genericStop, 11, 0));
        tripStages.add(new TransitTripStage(secondRoute, genericStop, 16, 0));
        tripStages.add(new TransitTripStage(secondRoute, genericStop, 51, 0));

        TransitTicket ticketWithTimeAndRouteConstraints = TransitTicket.builder(4, BigDecimal.valueOf(15)).setTimeLimit(15).build();
        ticketWithTimeAndRouteConstraints.getRoutePattern().addConstraint(RoutePattern.RouteAttribute.SHORT_NAME,
                Pattern.TextOperator.NOT_STARTS_WITH, "N");

        assertEquals(15, ticketWithTimeAndRouteConstraints.getTotalMinutesWhenValid(45, tripStages));
    }

    @Test
    public void shouldReturn45MinutesValidDueToMaxFaresLimitOnly() {
        Route firstRoute = new Route();
        firstRoute.setId(new FeedScopedId("ZTM", "105"));
        firstRoute.setShortName("105");
        Route secondRoute = new Route();
        secondRoute.setId(new FeedScopedId("ZTM", "13"));
        secondRoute.setShortName("13");

        Stop genericStop = new Stop();
        genericStop.setId(new FeedScopedId());

        List<TransitTripStage> tripStages = new ArrayList<>();

        /*

        Routes used in the tested itinerary:

           10 min   5 min           35 min
        |----------|-----|---------------------------|
           105      walk             13

         */
        tripStages.add(new TransitTripStage(firstRoute, genericStop, 1, 0));
        tripStages.add(new TransitTripStage(firstRoute, genericStop, 11, 0));
        tripStages.add(new TransitTripStage(secondRoute, genericStop, 16, 0));
        tripStages.add(new TransitTripStage(secondRoute, genericStop, 51, 0));

        TransitTicket ticketWithMaxFaresConstraints = TransitTicket.builder(4, BigDecimal.valueOf(15)).setFaresNumberLimit(2).build();

        assertEquals(45, ticketWithMaxFaresConstraints.getTotalMinutesWhenValid(45, tripStages));
    }

    @Test
    public void shouldReturn30MinutesValidDueToMaxFaresLimitOnly() {
        Route firstRoute = new Route();
        firstRoute.setId(new FeedScopedId("ZTM", "105"));
        firstRoute.setShortName("105");
        Route secondRoute = new Route();
        secondRoute.setId(new FeedScopedId("ZTM", "13"));
        secondRoute.setShortName("13");

        Stop genericStop = new Stop();
        genericStop.setId(new FeedScopedId());

        List<TransitTripStage> tripStages = new ArrayList<>();

        /*

        Routes used in the tested itinerary:

           10 min   5 min           35 min
        |----------|-----|---------------------------|
           105      walk             13

         */
        tripStages.add(new TransitTripStage(firstRoute, genericStop, 1, 0));
        tripStages.add(new TransitTripStage(firstRoute, genericStop, 11, 0));
        tripStages.add(new TransitTripStage(secondRoute, genericStop, 16, 0));
        tripStages.add(new TransitTripStage(secondRoute, genericStop, 51, 0));

        TransitTicket ticketWithMaxFaresConstraints = TransitTicket.builder(4, BigDecimal.valueOf(15)).setFaresNumberLimit(1).build();

        assertEquals(30, ticketWithMaxFaresConstraints.getTotalMinutesWhenValid(45, tripStages));
    }

    @Test
    public void shouldReturn30MinutesValidDueToMaxFaresLimit() {
        Route firstRoute = new Route();
        firstRoute.setId(new FeedScopedId("ZTM", "105"));
        firstRoute.setShortName("105");
        Route secondRoute = new Route();
        secondRoute.setId(new FeedScopedId("ZTM", "13"));
        secondRoute.setShortName("13");

        Stop genericStop = new Stop();
        genericStop.setId(new FeedScopedId());

        List<TransitTripStage> tripStages = new ArrayList<>();

        /*

        Routes used in the tested itinerary:

           10 min   5 min           35 min
        |----------|-----|---------------------------|
           105      walk             13

         */
        tripStages.add(new TransitTripStage(firstRoute, genericStop, 1, 0));
        tripStages.add(new TransitTripStage(firstRoute, genericStop, 11, 0));
        tripStages.add(new TransitTripStage(secondRoute, genericStop, 16, 0));
        tripStages.add(new TransitTripStage(secondRoute, genericStop, 51, 0));

        TransitTicket ticketWithRouteAndMaxFaresConstraints = TransitTicket.builder(4, BigDecimal.valueOf(15)).setFaresNumberLimit(1).build();
        ticketWithRouteAndMaxFaresConstraints.getRoutePattern().addConstraint(RoutePattern.RouteAttribute.SHORT_NAME,
                Pattern.TextOperator.NOT_STARTS_WITH, "N");

        assertEquals(30, ticketWithRouteAndMaxFaresConstraints.getTotalMinutesWhenValid(45, tripStages));
    }

    @Test
    public void shouldReturn30MinutesValidDueToFareSwitchingRules() {
        Route firstRoute = new Route();
        firstRoute.setId(new FeedScopedId("ZTM", "105"));
        firstRoute.setShortName("105");
        Route secondRoute = new Route();
        secondRoute.setId(new FeedScopedId("ZTM", "13"));
        secondRoute.setShortName("13");

        Stop genericStop = new Stop();
        genericStop.setId(new FeedScopedId());

        List<TransitTripStage> tripStages = new ArrayList<>();

        /*

        Routes used in the tested itinerary:

           10 min   5 min           35 min
        |----------|-----|---------------------------|
           105      walk             13

         */
        tripStages.add(new TransitTripStage(firstRoute, genericStop, 1, 0));
        tripStages.add(new TransitTripStage(firstRoute, genericStop, 11, 0));
        tripStages.add(new TransitTripStage(secondRoute, genericStop, 16, 0));
        tripStages.add(new TransitTripStage(secondRoute, genericStop, 51, 0));

        TransitTicket ticketWithRouteAndMaxFaresConstraints = TransitTicket.builder(4, BigDecimal.valueOf(15)).setFaresNumberLimit(2).build();
        RoutePattern previousRoutePattern = new RoutePattern();
        previousRoutePattern.addConstraint(RoutePattern.RouteAttribute.SHORT_NAME, Pattern.TextOperator.IN, "13");
        RoutePattern futureRoutePattern = new RoutePattern();
        futureRoutePattern.addConstraint(RoutePattern.RouteAttribute.SHORT_NAME, Pattern.TextOperator.IN, "105");
        ticketWithRouteAndMaxFaresConstraints.getFareSwitchPatterns().add(
                new FareSwitchPattern(previousRoutePattern, futureRoutePattern, null, null, false));

        assertEquals(30, ticketWithRouteAndMaxFaresConstraints.getTotalMinutesWhenValid(45, tripStages));
    }

    @Test
    public void shouldReturn45MinutesValidRegardlessFareSwitchingRules() {
        Route firstRoute = new Route();
        firstRoute.setId(new FeedScopedId("ZTM", "105"));
        firstRoute.setShortName("105");
        Route secondRoute = new Route();
        secondRoute.setId(new FeedScopedId("ZTM", "13"));
        secondRoute.setShortName("13");

        Stop genericStop = new Stop();
        genericStop.setId(new FeedScopedId());

        List<TransitTripStage> tripStages = new ArrayList<>();

        /*

        Routes used in the tested itinerary:

           10 min   5 min           35 min
        |----------|-----|---------------------------|
           105      walk             13

         */
        tripStages.add(new TransitTripStage(firstRoute, genericStop, 1, 0));
        tripStages.add(new TransitTripStage(firstRoute, genericStop, 11, 0));
        tripStages.add(new TransitTripStage(secondRoute, genericStop, 16, 0));
        tripStages.add(new TransitTripStage(secondRoute, genericStop, 51, 0));

        TransitTicket ticketWithRouteAndMaxFaresConstraints = TransitTicket.builder(4, BigDecimal.valueOf(15)).setFaresNumberLimit(2).build();
        RoutePattern previousRoutePattern = new RoutePattern();
        previousRoutePattern.addConstraint(RoutePattern.RouteAttribute.SHORT_NAME, Pattern.TextOperator.IN, "105");
        RoutePattern futureRoutePattern = new RoutePattern();
        futureRoutePattern.addConstraint(RoutePattern.RouteAttribute.SHORT_NAME, Pattern.TextOperator.IN, "13");
        ticketWithRouteAndMaxFaresConstraints.getFareSwitchPatterns().add(
                new FareSwitchPattern(previousRoutePattern, futureRoutePattern, null, null, false));

        assertEquals(45, ticketWithRouteAndMaxFaresConstraints.getTotalMinutesWhenValid(45, tripStages));
    }

    @Test
    public void shouldReturn30MinutesValidDueToRouteConstraints() {
        Route firstRoute = new Route();
        firstRoute.setId(new FeedScopedId("ZTM", "N30"));
        firstRoute.setShortName("N30");
        Route secondRoute = new Route();
        secondRoute.setId(new FeedScopedId("ZTM", "13"));
        secondRoute.setShortName("13");

        Stop genericStop = new Stop();
        genericStop.setId(new FeedScopedId());

        List<TransitTripStage> tripStages = new ArrayList<>();

        /*

        Routes used in the tested itinerary:

           10 min   5 min           35 min
        |----------|-----|---------------------------|
           N30      walk             13

         */
        tripStages.add(new TransitTripStage(firstRoute, genericStop, 1, 0));
        tripStages.add(new TransitTripStage(firstRoute, genericStop, 11, 0));
        tripStages.add(new TransitTripStage(secondRoute, genericStop, 16, 0));
        tripStages.add(new TransitTripStage(secondRoute, genericStop, 50, 0));

        TransitTicket ticketWithRouteAndMaxFaresConstraints = TransitTicket.builder(4, BigDecimal.valueOf(15)).setFaresNumberLimit(2).build();
        ticketWithRouteAndMaxFaresConstraints.getRoutePattern().addConstraint(RoutePattern.RouteAttribute.SHORT_NAME,
                Pattern.TextOperator.NOT_STARTS_WITH, "N");
        assertEquals(30, ticketWithRouteAndMaxFaresConstraints.getTotalMinutesWhenValid(45, tripStages));
    }

    @Test
    public void shouldReturn5MinutesValid() {
        Route firstRoute = new Route();
        firstRoute.setId(new FeedScopedId("ZTM", "105"));
        firstRoute.setShortName("105");
        Route secondRoute = new Route();
        secondRoute.setId(new FeedScopedId("ZTM", "13"));
        secondRoute.setShortName("13");

        Stop stop1 = new Stop();
        stop1.setZoneId("2");
        stop1.setId(new FeedScopedId());
        Stop stop8 = new Stop();
        stop8.setZoneId("2");
        stop8.setId(new FeedScopedId());
        Stop stop11 = new Stop();
        stop11.setZoneId("2");
        stop11.setId(new FeedScopedId());
        Stop stop16 = new Stop();
        stop16.setZoneId("2");
        stop16.setId(new FeedScopedId());
        Stop stop41 = new Stop();
        stop41.setZoneId("1/2");
        stop41.setId(new FeedScopedId());
        Stop stop47 = new Stop();
        stop47.setZoneId("1");
        stop47.setId(new FeedScopedId());
        Stop stop51 = new Stop();
        stop51.setZoneId("1");
        stop51.setId(new FeedScopedId());

        List<TransitTripStage> tripStages = new ArrayList<>();

        /*

        Routes used in the tested itinerary:

        |  10 min   |5 min|             35 min                  |     Travel time
        |<--------->|<--->|<----------------------------------->|
        0       7   10    15                        40     46   50    Arrive at stop time (minutes)
        |-------|---|-----|-------------------------|------|----|
        |           |     |                                     |
        |   105     |walk |               13                    |     Mean of transport

         */
        tripStages.add(new TransitTripStage(firstRoute, stop1, 1, 0));
        tripStages.add(new TransitTripStage(firstRoute, stop8, 8, 0));
        tripStages.add(new TransitTripStage(firstRoute, stop11, 11, 0));
        tripStages.add(new TransitTripStage(secondRoute, stop16, 16, 0));
        tripStages.add(new TransitTripStage(secondRoute, stop41, 41, 0));
        tripStages.add(new TransitTripStage(secondRoute, stop47, 47, 0));
        tripStages.add(new TransitTripStage(secondRoute, stop51, 51, 0));

        TransitTicket ticketWithRouteConstraints = TransitTicket.builder(4, BigDecimal.valueOf(15)).build();
        ticketWithRouteConstraints.getStopPattern().addConstraint(StopPattern.StopAttribute.ZONE, Pattern.TextOperator.IN, "1");
        ticketWithRouteConstraints.getStopPattern().addConstraint(StopPattern.StopAttribute.ZONE, Pattern.TextOperator.IN, "1/2");

        assertEquals(5, ticketWithRouteConstraints.getTotalMinutesWhenValid(45, tripStages));
    }

    @Test
    public void shouldReturn0MinutesValid() {
        Route firstRoute = new Route();
        firstRoute.setId(new FeedScopedId("ZTM", "105"));
        firstRoute.setShortName("105");
        Route secondRoute = new Route();
        secondRoute.setId(new FeedScopedId("ZTM", "13"));
        secondRoute.setShortName("13");

        Stop stop1 = new Stop();
        stop1.setZoneId("2");
        stop1.setId(new FeedScopedId());
        Stop stop8 = new Stop();
        stop8.setZoneId("2");
        stop8.setId(new FeedScopedId());
        Stop stop11 = new Stop();
        stop11.setZoneId("2");
        stop11.setId(new FeedScopedId());
        Stop stop16 = new Stop();
        stop16.setZoneId("2");
        stop16.setId(new FeedScopedId());
        Stop stop41 = new Stop();
        stop41.setZoneId("1/2");
        stop41.setId(new FeedScopedId());
        Stop stop47 = new Stop();
        stop47.setZoneId("1");
        stop47.setId(new FeedScopedId());
        Stop stop51 = new Stop();
        stop51.setZoneId("1");
        stop51.setId(new FeedScopedId());

        List<TransitTripStage> tripStages = new ArrayList<>();

        /*

        Routes used in the tested itinerary:

        |  10 min   |5 min|             35 min                  |     Travel time
        |<--------->|<--->|<----------------------------------->|
        0       7   10    15                        40     46   50    Arrive at stop time (minutes)
        |-------|---|-----|-------------------------|------|----|
        |           |     |                                     |
        |   105     |walk |               13                    |     Mean of transport

         */

        tripStages.add(new TransitTripStage(firstRoute, stop1, 1, 0));
        tripStages.add(new TransitTripStage(firstRoute, stop8, 8, 0));
        tripStages.add(new TransitTripStage(firstRoute, stop11, 11, 0));
        tripStages.add(new TransitTripStage(secondRoute, stop16, 16, 0));
        tripStages.add(new TransitTripStage(secondRoute, stop41, 41, 0));
        tripStages.add(new TransitTripStage(secondRoute, stop47, 47, 0));
        tripStages.add(new TransitTripStage(secondRoute, stop51, 51, 0));

        TransitTicket ticketWithRouteConstraints = TransitTicket.builder(4, BigDecimal.valueOf(15)).build();
        ticketWithRouteConstraints.getStopPattern().addConstraint(StopPattern.StopAttribute.ZONE, Pattern.TextOperator.IN, "2");
        ticketWithRouteConstraints.getStopPattern().addConstraint(StopPattern.StopAttribute.ZONE, Pattern.TextOperator.IN, "1/2");

        assertEquals(0, ticketWithRouteConstraints.getTotalMinutesWhenValid(45, tripStages));
    }

    @Test
    public void shouldReturn0MinutesValidDueToRouteConstraints() {
        Route firstRoute = new Route();
        firstRoute.setId(new FeedScopedId("ZTM", "105"));
        firstRoute.setShortName("105");
        Route secondRoute = new Route();
        secondRoute.setId(new FeedScopedId("ZTM", "13"));
        secondRoute.setShortName("13");

        Stop stop1 = new Stop();
        stop1.setZoneId("2");
        stop1.setId(new FeedScopedId());
        Stop stop8 = new Stop();
        stop8.setZoneId("2");
        stop8.setId(new FeedScopedId());
        Stop stop11 = new Stop();
        stop11.setZoneId("2");
        stop11.setId(new FeedScopedId());
        Stop stop16 = new Stop();
        stop16.setZoneId("2");
        stop16.setId(new FeedScopedId());
        Stop stop41 = new Stop();
        stop41.setZoneId("1/2");
        stop41.setId(new FeedScopedId());
        Stop stop47 = new Stop();
        stop47.setZoneId("1");
        stop47.setId(new FeedScopedId());
        Stop stop51 = new Stop();
        stop51.setZoneId("1");
        stop51.setId(new FeedScopedId());

        List<TransitTripStage> tripStages = new ArrayList<>();

        /*

        Routes used in the tested itinerary:

        |  10 min   |5 min|             35 min                  |     Travel time
        |<--------->|<--->|<----------------------------------->|
        0       7   10    15                        40     46   50    Arrive at stop time (minutes)
        |-------|---|-----|-------------------------|------|----|
        |           |     |                                     |
        |   105     |walk |               13                    |     Mean of transport

         */
        tripStages.add(new TransitTripStage(firstRoute, stop1, 1, 0));
        tripStages.add(new TransitTripStage(firstRoute, stop8, 8, 0));
        tripStages.add(new TransitTripStage(firstRoute, stop11, 11, 0));
        tripStages.add(new TransitTripStage(secondRoute, stop16, 16, 0));
        tripStages.add(new TransitTripStage(secondRoute, stop41, 41, 0));
        tripStages.add(new TransitTripStage(secondRoute, stop47, 47, 0));
        tripStages.add(new TransitTripStage(secondRoute, stop51, 51, 0));

        TransitTicket ticketWithRouteAndStopConstraints = TransitTicket.builder(4, BigDecimal.valueOf(15)).build();
        ticketWithRouteAndStopConstraints.getStopPattern().addConstraint(StopPattern.StopAttribute.ZONE, Pattern.TextOperator.IN, "1");
        ticketWithRouteAndStopConstraints.getStopPattern().addConstraint(StopPattern.StopAttribute.ZONE, Pattern.TextOperator.IN, "1/2");
        ticketWithRouteAndStopConstraints.getRoutePattern().addConstraint(RoutePattern.RouteAttribute.SHORT_NAME,
                Pattern.TextOperator.STARTS_WITH, "N");

        assertEquals(0, ticketWithRouteAndStopConstraints.getTotalMinutesWhenValid(45, tripStages));
    }

    @Test
    public void shouldReturn45MinutesValidDueToNoConstraints() {
        Route firstRoute = new Route();
        firstRoute.setId(new FeedScopedId("ZTM", "105"));
        firstRoute.setShortName("105");
        Route secondRoute = new Route();
        secondRoute.setId(new FeedScopedId("ZTM", "13"));
        secondRoute.setShortName("13");

        Stop stop1 = new Stop();
        stop1.setZoneId("2");
        stop1.setId(new FeedScopedId());
        Stop stop8 = new Stop();
        stop8.setZoneId("2");
        stop8.setId(new FeedScopedId());
        Stop stop11 = new Stop();
        stop11.setZoneId("2");
        stop11.setId(new FeedScopedId());
        Stop stop16 = new Stop();
        stop16.setZoneId("2");
        stop16.setId(new FeedScopedId());
        Stop stop41 = new Stop();
        stop41.setZoneId("1/2");
        stop41.setId(new FeedScopedId());
        Stop stop47 = new Stop();
        stop47.setZoneId("1");
        stop47.setId(new FeedScopedId());
        Stop stop51 = new Stop();
        stop51.setZoneId("1");
        stop51.setId(new FeedScopedId());

        List<TransitTripStage> tripStages = new ArrayList<>();

        /*

        Routes used in the tested itinerary:

        |  10 min   |5 min|             35 min                  |     Travel time
        |<--------->|<--->|<----------------------------------->|
        0       7   10    15                        40     46   50    Arrive at stop time (minutes)
        |-------|---|-----|-------------------------|------|----|
        |           |     |                                     |
        |   105     |walk |               13                    |     Mean of transport

         */
        tripStages.add(new TransitTripStage(firstRoute, stop1, 1, 0));
        tripStages.add(new TransitTripStage(firstRoute, stop8, 8, 10));
        tripStages.add(new TransitTripStage(firstRoute, stop11, 11, 5));
        tripStages.add(new TransitTripStage(secondRoute, stop16, 16, 0));
        tripStages.add(new TransitTripStage(secondRoute, stop41, 41, 40));
        tripStages.add(new TransitTripStage(secondRoute, stop47, 47, 7));
        tripStages.add(new TransitTripStage(secondRoute, stop51, 51, 5));

        TransitTicket ticketWithNoConstraints = TransitTicket.builder(4, BigDecimal.valueOf(15)).build();

        assertEquals(45, ticketWithNoConstraints.getTotalMinutesWhenValid(45, tripStages));
    }

    @Test
    public void shouldReturn5MinutesValidDueToDistanceConstraints() {
        Route firstRoute = new Route();
        firstRoute.setId(new FeedScopedId("ZTM", "105"));
        firstRoute.setShortName("105");
        Route secondRoute = new Route();
        secondRoute.setId(new FeedScopedId("ZTM", "13"));
        secondRoute.setShortName("13");

        Stop stop1 = new Stop();
        stop1.setId(new FeedScopedId());
        Stop stop8 = new Stop();
        stop8.setId(new FeedScopedId());
        Stop stop11 = new Stop();
        stop11.setId(new FeedScopedId());
        Stop stop16 = new Stop();
        stop16.setId(new FeedScopedId());
        Stop stop41 = new Stop();
        stop41.setId(new FeedScopedId());
        Stop stop47 = new Stop();
        stop47.setId(new FeedScopedId());
        Stop stop51 = new Stop();
        stop51.setId(new FeedScopedId());

        List<TransitTripStage> tripStages = new ArrayList<>();

        /*

        Routes used in the tested itinerary:

        |  10 min   |5 min|             35 min                  |     Travel time
        |<--------->|<--->|<----------------------------------->|
        0       7   10    15                        40     46   50    Arrive at stop time (minutes)
        |-------|---|-----|-------------------------|------|----|
        |           |     |                                     |
        |   105     |walk |               13                    |     Mean of transport

         */
        tripStages.add(new TransitTripStage(firstRoute, stop1, 1, 0));
        tripStages.add(new TransitTripStage(firstRoute, stop8, 8, 10));
        tripStages.add(new TransitTripStage(firstRoute, stop11, 11, 5));
        tripStages.add(new TransitTripStage(secondRoute, stop16, 16, 0));
        tripStages.add(new TransitTripStage(secondRoute, stop41, 41, 40));
        tripStages.add(new TransitTripStage(secondRoute, stop47, 47, 7));
        tripStages.add(new TransitTripStage(secondRoute, stop51, 51, 5));

        TransitTicket ticketWithDistanceConstraints = TransitTicket.builder(4, BigDecimal.valueOf(15)).setDistanceLimit(10).build();

        assertEquals(5, ticketWithDistanceConstraints.getTotalMinutesWhenValid(45, tripStages));
    }

    @Test
    public void shouldReturn30MinutesValidDueToDistanceConstraints() {
        Route firstRoute = new Route();
        firstRoute.setId(new FeedScopedId("ZTM", "105"));
        firstRoute.setShortName("105");
        Route secondRoute = new Route();
        secondRoute.setId(new FeedScopedId("ZTM", "13"));
        secondRoute.setShortName("13");

        Stop stop1 = new Stop();
        stop1.setId(new FeedScopedId());
        Stop stop8 = new Stop();
        stop8.setId(new FeedScopedId());
        Stop stop11 = new Stop();
        stop11.setId(new FeedScopedId());
        Stop stop16 = new Stop();
        stop16.setId(new FeedScopedId());
        Stop stop41 = new Stop();
        stop41.setId(new FeedScopedId());
        Stop stop47 = new Stop();
        stop47.setId(new FeedScopedId());
        Stop stop51 = new Stop();
        stop51.setId(new FeedScopedId());

        List<TransitTripStage> tripStages = new ArrayList<>();

        /*

        Routes used in the tested itinerary:

        |  10 min   |5 min|             35 min                  |     Travel time
        |<--------->|<--->|<----------------------------------->|
        0       7   10    15                        40     46   50    Arrive at stop time (minutes)
        |-------|---|-----|-------------------------|------|----|
        |           |     |                                     |
        |   105     |walk |               13                    |     Mean of transport

         */
        tripStages.add(new TransitTripStage(firstRoute, stop1, 1, 0));
        tripStages.add(new TransitTripStage(firstRoute, stop8, 8, 10));
        tripStages.add(new TransitTripStage(firstRoute, stop11, 11, 5));
        tripStages.add(new TransitTripStage(secondRoute, stop16, 16, 0));
        tripStages.add(new TransitTripStage(secondRoute, stop41, 41, 40));
        tripStages.add(new TransitTripStage(secondRoute, stop47, 47, 7));
        tripStages.add(new TransitTripStage(secondRoute, stop51, 51, 5));

        TransitTicket ticketWithDistanceConstraints = TransitTicket.builder(4, BigDecimal.valueOf(15)).setDistanceLimit(50).build();

        assertEquals(30, ticketWithDistanceConstraints.getTotalMinutesWhenValid(45, tripStages));
    }

    @Test
    public void shouldReturn38MinutesValidDueToDistanceConstraints() {
        Route firstRoute = new Route();
        firstRoute.setId(new FeedScopedId("ZTM", "105"));
        firstRoute.setShortName("105");
        Route secondRoute = new Route();
        secondRoute.setId(new FeedScopedId("ZTM", "13"));
        secondRoute.setShortName("13");

        Stop stop1 = new Stop();
        stop1.setId(new FeedScopedId());
        Stop stop8 = new Stop();
        stop8.setId(new FeedScopedId());
        Stop stop11 = new Stop();
        stop11.setId(new FeedScopedId());
        Stop stop16 = new Stop();
        stop16.setId(new FeedScopedId());
        Stop stop41 = new Stop();
        stop41.setId(new FeedScopedId());
        Stop stop47 = new Stop();
        stop47.setId(new FeedScopedId());
        Stop stop51 = new Stop();
        stop51.setId(new FeedScopedId());

        List<TransitTripStage> tripStages = new ArrayList<>();

        /*

        Routes used in the tested itinerary:

        |  10 min   |5 min|             35 min                  |     Travel time
        |<--------->|<--->|<----------------------------------->|
        0       7   10    15                        40     46   50    Arrive at stop time (minutes)
        |-------|---|-----|-------------------------|------|----|
        |           |     |                                     |
        |   105     |walk |               13                    |     Mean of transport

         */
        tripStages.add(new TransitTripStage(firstRoute, stop1, 1, 0));
        tripStages.add(new TransitTripStage(firstRoute, stop8, 8, 10));
        tripStages.add(new TransitTripStage(firstRoute, stop11, 11, 5));
        tripStages.add(new TransitTripStage(secondRoute, stop16, 16, 0));
        tripStages.add(new TransitTripStage(secondRoute, stop41, 41, 40));
        tripStages.add(new TransitTripStage(secondRoute, stop47, 47, 7));
        tripStages.add(new TransitTripStage(secondRoute, stop51, 51, 5));

        TransitTicket ticketWithDistanceConstraints = TransitTicket.builder(4, BigDecimal.valueOf(15)).setDistanceLimit(55).build();

        assertEquals(38, ticketWithDistanceConstraints.getTotalMinutesWhenValid(45, tripStages));
    }

    @Test
    public void shouldReturn45MinutesValidDueToDistanceConstraints() {
        Route firstRoute = new Route();
        firstRoute.setId(new FeedScopedId("ZTM", "105"));
        firstRoute.setShortName("105");
        Route secondRoute = new Route();
        secondRoute.setId(new FeedScopedId("ZTM", "13"));
        secondRoute.setShortName("13");

        Stop stop1 = new Stop();
        stop1.setId(new FeedScopedId());
        Stop stop8 = new Stop();
        stop8.setId(new FeedScopedId());
        Stop stop11 = new Stop();
        stop11.setId(new FeedScopedId());
        Stop stop16 = new Stop();
        stop16.setId(new FeedScopedId());
        Stop stop41 = new Stop();
        stop41.setId(new FeedScopedId());
        Stop stop47 = new Stop();
        stop47.setId(new FeedScopedId());
        Stop stop51 = new Stop();
        stop51.setId(new FeedScopedId());

        List<TransitTripStage> tripStages = new ArrayList<>();

        /*

        Routes used in the tested itinerary:

        |  10 min   |5 min|             35 min                  |     Travel time
        |<--------->|<--->|<----------------------------------->|
        0       7   10    15                        40     46   50    Arrive at stop time (minutes)
        |-------|---|-----|-------------------------|------|----|
        |           |     |                                     |
        |   105     |walk |               13                    |     Mean of transport

         */
        tripStages.add(new TransitTripStage(firstRoute, stop1, 1, 0));
        tripStages.add(new TransitTripStage(firstRoute, stop8, 8, 10));
        tripStages.add(new TransitTripStage(firstRoute, stop11, 11, 5));
        tripStages.add(new TransitTripStage(secondRoute, stop16, 16, 0));
        tripStages.add(new TransitTripStage(secondRoute, stop41, 41, 40));
        tripStages.add(new TransitTripStage(secondRoute, stop47, 47, 7));
        tripStages.add(new TransitTripStage(secondRoute, stop51, 51, 5));

        TransitTicket ticketWithDistanceConstraints = TransitTicket.builder(4, BigDecimal.valueOf(15)).setDistanceLimit(70).build();

        assertEquals(45, ticketWithDistanceConstraints.getTotalMinutesWhenValid(45, tripStages));
    }

}
