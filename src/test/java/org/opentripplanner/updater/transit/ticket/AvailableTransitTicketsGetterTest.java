package org.opentripplanner.updater.transit.ticket;

import org.junit.Test;
import org.opentripplanner.model.FeedScopedId;
import org.opentripplanner.model.Route;
import org.opentripplanner.model.Stop;
import org.opentripplanner.pricing.transit.ticket.TransitTicket;
import org.opentripplanner.pricing.transit.ticket.pattern.FareSwitchPattern;
import org.opentripplanner.pricing.transit.ticket.pattern.RoutePattern;
import org.opentripplanner.pricing.transit.ticket.pattern.StopPattern;
import org.opentripplanner.pricing.transit.trip.model.TransitTripStage;

import java.math.BigDecimal;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class AvailableTransitTicketsGetterTest {

    @Test
    public void shouldReturnSingleTicketWithFareSwitchRule() {
        AvailableTransitTicketsGetter transitTicketGetter = new AvailableTransitTicketsGetter();
        Set<TransitTicket> tickets = transitTicketGetter.getFromFile("src/test/resources/tickets/ticketWithFareSwitchRule.json");

        assertEquals(1, tickets.size());
        TransitTicket generatedTicket = tickets.iterator().next();

        assertEquals(1, generatedTicket.getId());
        assertEquals(-1, generatedTicket.getMaxMinutes());
        assertEquals(-1, generatedTicket.getMaxDistance());
        assertEquals(2, generatedTicket.getMaxFares());
        assertEquals(0, generatedTicket.getStandardPrice().compareTo(BigDecimal.valueOf(3)));
        assertEquals(20, generatedTicket.getAvailableFrom().getDayOfMonth());
        assertEquals(Month.DECEMBER, generatedTicket.getAvailableFrom().getMonth());
        assertEquals(2020, generatedTicket.getAvailableFrom().getYear());
        assertEquals(45, generatedTicket.getAvailableFrom().getSecond());
        assertEquals(35, generatedTicket.getAvailableFrom().getMinute());
        assertEquals(17, generatedTicket.getAvailableFrom().getHour());
        assertNull(generatedTicket.getAvailableTo());

        RoutePattern routePattern = generatedTicket.getRoutePattern("ZDMiKP");
        assertNotNull(routePattern);
        Route matchingRoute1 = new Route();
        matchingRoute1.setId(new FeedScopedId("ZDMiKP", "3"));
        matchingRoute1.setShortName("3");
        Route matchingRoute2 = new Route();
        matchingRoute2.setId(new FeedScopedId("ZDMiKP", "5"));
        matchingRoute2.setShortName("5");
        Route matchingRoute3 = new Route();
        matchingRoute3.setId(new FeedScopedId("ZDMiKP", "81"));
        matchingRoute3.setShortName("81");
        Route notMatchingRoute = new Route();
        notMatchingRoute.setId(new FeedScopedId("ZDMiKP", "15"));
        notMatchingRoute.setShortName("15");
        assertTrue(routePattern.matches(matchingRoute1));
        assertTrue(routePattern.matches(matchingRoute2));
        assertTrue(routePattern.matches(matchingRoute3));
        assertFalse(routePattern.matches(notMatchingRoute));

        FareSwitchPattern fareSwitchPattern = generatedTicket.getFareSwitchPatterns().get(0);
        assertTrue(fareSwitchPattern.getPreviousRoutePattern().matches(matchingRoute3));
        assertTrue(fareSwitchPattern.getFutureRoutePattern().matches(matchingRoute1));
        assertTrue(fareSwitchPattern.getFutureRoutePattern().matches(matchingRoute2));
        assertTrue(fareSwitchPattern.isReverseAllowed());
    }

    @Test
    public void shouldReturnSingleTicketWithTimeLimit() {
        AvailableTransitTicketsGetter transitTicketGetter = new AvailableTransitTicketsGetter();
        Set<TransitTicket> tickets = transitTicketGetter.getFromFile("src/test/resources/tickets/ticketWithTimeLimit.json");

        assertEquals(1, tickets.size());
        TransitTicket generatedTicket = tickets.iterator().next();

        assertEquals(20, generatedTicket.getId());
        assertEquals(20, generatedTicket.getMaxMinutes());
        assertEquals(-1, generatedTicket.getMaxDistance());
        assertEquals(-1, generatedTicket.getMaxFares());
        assertEquals(0, generatedTicket.getStandardPrice().compareTo(BigDecimal.valueOf(3.4)));
        assertEquals(20, generatedTicket.getAvailableFrom().getDayOfMonth());
        assertEquals(Month.DECEMBER, generatedTicket.getAvailableFrom().getMonth());
        assertEquals(2020, generatedTicket.getAvailableFrom().getYear());
        assertEquals(45, generatedTicket.getAvailableFrom().getSecond());
        assertEquals(35, generatedTicket.getAvailableFrom().getMinute());
        assertEquals(17, generatedTicket.getAvailableFrom().getHour());
        assertNull(generatedTicket.getAvailableTo());

        RoutePattern ztmRoutePattern = generatedTicket.getRoutePattern("ZTM");
        RoutePattern kmRoutePattern = generatedTicket.getRoutePattern("KM");
        assertNotNull(ztmRoutePattern);
        assertNotNull(kmRoutePattern);
        Route matchingRoute1 = new Route();
        matchingRoute1.setId(new FeedScopedId("ZTM", "3"));
        matchingRoute1.setShortName("3");
        Route notMatchingRoute = new Route();
        notMatchingRoute.setId(new FeedScopedId("ZTM", "L30"));
        notMatchingRoute.setShortName("L30");
        assertTrue(ztmRoutePattern.matches(matchingRoute1));
        assertFalse(ztmRoutePattern.matches(notMatchingRoute));

        matchingRoute1.setId(new FeedScopedId("KM", "RL5"));
        matchingRoute1.setShortName("RL5");
        notMatchingRoute.setId(new FeedScopedId("KM", "1"));
        notMatchingRoute.setShortName("1");
        assertTrue(kmRoutePattern.matches(matchingRoute1));
        assertFalse(kmRoutePattern.matches(notMatchingRoute));

        StopPattern kmStopPattern = generatedTicket.getStopPattern("KM");
        Stop matchingStop = new Stop();
        matchingStop.setId(new FeedScopedId());
        matchingStop.setName("Warszawa Płudy");
        Stop notMatchingStop = new Stop();
        notMatchingStop.setId(new FeedScopedId());
        notMatchingStop.setName("Plac Zawiszy");
        assertTrue(kmStopPattern.matches(matchingStop));
        assertFalse(kmStopPattern.matches(notMatchingStop));
    }

    @Test
    public void shouldReturnTicketWithNullAvailableFromDate() {
        AvailableTransitTicketsGetter transitTicketGetter = new AvailableTransitTicketsGetter();
        Set<TransitTicket> tickets = transitTicketGetter.getFromFile("src/test/resources/tickets/ticketWithInvalidDateFormat.json");
        assertNull(tickets.iterator().next().getAvailableFrom());
    }

    @Test
    public void shouldReturnEmptyTicketSet() {
        AvailableTransitTicketsGetter transitTicketGetter = new AvailableTransitTicketsGetter();
        Set<TransitTicket> tickets = transitTicketGetter.getFromFile("src/test/resources/tickets/nonExistingFile.json");
        assertTrue(tickets.isEmpty());
    }

    @Test
    public void shouldReturnEmptyTicketSetDueToInvalidTicketStructure() {
        AvailableTransitTicketsGetter transitTicketGetter = new AvailableTransitTicketsGetter();
        Set<TransitTicket> tickets = transitTicketGetter.getFromFile("src/test/resources/tickets/ticketWithInvalidStructure.json");
        assertTrue(tickets.isEmpty());
    }

    //----------------------------------------------------------------------------------------------------
    //Tests for Warsaw below
    //----------------------------------------------------------------------------------------------------

    @Test
    public void shouldReturn7WarsawTickets() {
        AvailableTransitTicketsGetter transitTicketGetter = new AvailableTransitTicketsGetter();
        Set<TransitTicket> tickets = transitTicketGetter.getFromFile("src/test/resources/tickets/warsaw/availableTickets.json");

        assertEquals(7, tickets.size());
    }

    @Test
    public void shouldReturnSingleWarsaw20MinutesTicket() {
        AvailableTransitTicketsGetter transitTicketGetter = new AvailableTransitTicketsGetter();
        Set<TransitTicket> tickets = transitTicketGetter.getFromFile("src/test/resources/tickets/warsaw/20minTicket.json");

        assertEquals(1, tickets.size());
        TransitTicket generatedTicket = tickets.iterator().next();

        assertEquals(1, generatedTicket.getId());
        assertEquals(20, generatedTicket.getMaxMinutes());
        assertEquals(-1, generatedTicket.getMaxDistance());
        assertEquals(-1, generatedTicket.getMaxFares());
        assertEquals(0, generatedTicket.getStandardPrice().compareTo(BigDecimal.valueOf(3.4)));
        assertNull(generatedTicket.getAvailableFrom());
        assertNull(generatedTicket.getAvailableTo());

        RoutePattern ztmRoutePattern = generatedTicket.getRoutePattern("0");
        assertNotNull(ztmRoutePattern);

        Route route = new Route();
        route.setId(new FeedScopedId("0", "105"));
        route.setShortName("105");
        Stop stop1 = new Stop();
        stop1.setId(new FeedScopedId());
        Stop stop8 = new Stop();
        stop8.setId(new FeedScopedId());
        Stop stop11 = new Stop();
        stop11.setId(new FeedScopedId());
        List<TransitTripStage> tripStages = new ArrayList<>();

        /*

        Routes used in the tested itinerary:

        |  10 min   |    Travel time
        |<--------->|
        0       7   10   Arrive at stop time (minutes)
        |-------|---|
        |           |
        |   105     |    Mean of transport

         */
        tripStages.add(new TransitTripStage(route, stop1, 1, 0));
        tripStages.add(new TransitTripStage(route, stop8, 8, 10));
        tripStages.add(new TransitTripStage(route, stop11, 11, 5));
        assertEquals(11, generatedTicket.getTotalMinutesWhenValid(11, tripStages));

        route.getId().setId("L14");
        assertEquals(0, generatedTicket.getTotalMinutesWhenValid(11, tripStages));

        //Setting invalid id for route - ticket is not applicable
        route.getId().setId("105");
        route.getId().setAgencyId("ZTM");
        assertEquals(0, generatedTicket.getTotalMinutesWhenValid(11, tripStages));

    }

    @Test
    public void shouldReturnSingleWarsaw75MinutesTicket() {
        AvailableTransitTicketsGetter transitTicketGetter = new AvailableTransitTicketsGetter();
        Set<TransitTicket> tickets = transitTicketGetter.getFromFile("src/test/resources/tickets/warsaw/75minTicket.json");

        assertEquals(1, tickets.size());
        TransitTicket generatedTicket = tickets.iterator().next();

        assertEquals(2, generatedTicket.getId());
        assertEquals(75, generatedTicket.getMaxMinutes());
        assertEquals(-1, generatedTicket.getMaxDistance());
        assertEquals(-1, generatedTicket.getMaxFares());
        assertEquals(0, generatedTicket.getStandardPrice().compareTo(BigDecimal.valueOf(4.4)));
        assertNull(generatedTicket.getAvailableFrom());
        assertNull(generatedTicket.getAvailableTo());

        RoutePattern ztmRoutePattern = generatedTicket.getRoutePattern("0");
        assertNotNull(ztmRoutePattern);

        Route route = new Route();
        route.setId(new FeedScopedId("0", "105"));
        route.setShortName("105");
        Stop stop1 = new Stop();
        stop1.setZoneId("1");
        stop1.setId(new FeedScopedId());
        Stop stop8 = new Stop();
        stop8.setZoneId("1");
        stop8.setId(new FeedScopedId());
        Stop stop11 = new Stop();
        stop11.setZoneId("1");
        stop11.setId(new FeedScopedId());
        List<TransitTripStage> tripStages = new ArrayList<>();

        /*

        Routes used in the tested itinerary:

        |  10 min   |    Travel time
        |<--------->|
        0       7   10   Arrive at stop time (minutes)
        |-------|---|
        |           |
        |   105     |    Mean of transport

         */
        tripStages.add(new TransitTripStage(route, stop1, 1, 0));
        tripStages.add(new TransitTripStage(route, stop8, 8, 10));
        tripStages.add(new TransitTripStage(route, stop11, 11, 5));
        assertEquals(11, generatedTicket.getTotalMinutesWhenValid(11, tripStages));

        route.getId().setId("L14");
        assertEquals(0, generatedTicket.getTotalMinutesWhenValid(11, tripStages));

        route.getId().setId("105");
        stop1.setZoneId("2");
        stop8.setZoneId("1/2");
        //not valid for the entire trip due to zone id change
        assertEquals(4, generatedTicket.getTotalMinutesWhenValid(11, tripStages));

        //Setting invalid id for route - ticket is not applicable
        route.getId().setAgencyId("ZTM");
        assertEquals(0, generatedTicket.getTotalMinutesWhenValid(11, tripStages));
    }

    @Test
    public void shouldReturnSingleWarsaw75MinutesTicketSingleFareVersion() {
        AvailableTransitTicketsGetter transitTicketGetter = new AvailableTransitTicketsGetter();
        Set<TransitTicket> tickets = transitTicketGetter.getFromFile("src/test/resources/tickets/warsaw/75minTicket_singleFare.json");

        assertEquals(1, tickets.size());
        TransitTicket generatedTicket = tickets.iterator().next();

        assertEquals(3, generatedTicket.getId());
        assertEquals(-1, generatedTicket.getMaxMinutes());
        assertEquals(-1, generatedTicket.getMaxDistance());
        assertEquals(1, generatedTicket.getMaxFares());
        assertEquals(0, generatedTicket.getStandardPrice().compareTo(BigDecimal.valueOf(4.4)));
        assertNull(generatedTicket.getAvailableFrom());
        assertNull(generatedTicket.getAvailableTo());

        RoutePattern ztmRoutePattern = generatedTicket.getRoutePattern("0");
        assertNotNull(ztmRoutePattern);

        Route route = new Route();
        route.setId(new FeedScopedId("0", "105"));
        route.setShortName("105");
        Stop stop1 = new Stop();
        stop1.setZoneId("1");
        stop1.setId(new FeedScopedId());
        Route route2 = new Route();
        route2.setId(new FeedScopedId("0", "4"));
        route2.setShortName("4");
        Stop stop8 = new Stop();
        stop8.setZoneId("1");
        stop8.setId(new FeedScopedId());
        Stop stop11 = new Stop();
        stop11.setZoneId("1");
        stop11.setId(new FeedScopedId());
        List<TransitTripStage> tripStages = new ArrayList<>();

        /*

        Routes used in the tested itinerary:

        |  10 min   |    Travel time
        |<--------->|
        0       7   10   Arrive at stop time (minutes)
        |-------|---|
        |       |   |
        |  105  | 4 |    Mean of transport

         */
        tripStages.add(new TransitTripStage(route, stop1, 1, 0));
        tripStages.add(new TransitTripStage(route, stop8, 8, 10));
        tripStages.add(new TransitTripStage(route, stop8, 8, 10));
        tripStages.add(new TransitTripStage(route, stop11, 11, 5));
        assertEquals(11, generatedTicket.getTotalMinutesWhenValid(11, tripStages));

        route.getId().setId("L14");
        assertEquals(0, generatedTicket.getTotalMinutesWhenValid(11, tripStages));

        route.getId().setId("105");
        tripStages = new ArrayList<>();
        tripStages.add(new TransitTripStage(route, stop1, 1, 0));
        tripStages.add(new TransitTripStage(route, stop8, 8, 10));
        tripStages.add(new TransitTripStage(route2, stop8, 8, 10));
        tripStages.add(new TransitTripStage(route2, stop11, 11, 5));
        assertEquals(4, generatedTicket.getTotalMinutesWhenValid(11, tripStages));

        //Setting invalid id for route - ticket is not applicable
        route2.getId().setAgencyId("ZTM");
        assertEquals(0, generatedTicket.getTotalMinutesWhenValid(11, tripStages));
    }

    @Test
    public void shouldReturnSingleWarsaw90MinutesTicket() {
        AvailableTransitTicketsGetter transitTicketGetter = new AvailableTransitTicketsGetter();
        Set<TransitTicket> tickets = transitTicketGetter.getFromFile("src/test/resources/tickets/warsaw/90minTicket.json");

        assertEquals(1, tickets.size());
        TransitTicket generatedTicket = tickets.iterator().next();

        assertEquals(4, generatedTicket.getId());
        assertEquals(90, generatedTicket.getMaxMinutes());
        assertEquals(-1, generatedTicket.getMaxDistance());
        assertEquals(-1, generatedTicket.getMaxFares());
        assertEquals(0, generatedTicket.getStandardPrice().compareTo(BigDecimal.valueOf(7)));
        assertNull(generatedTicket.getAvailableFrom());
        assertNull(generatedTicket.getAvailableTo());

        RoutePattern ztmRoutePattern = generatedTicket.getRoutePattern("0");
        assertNotNull(ztmRoutePattern);

        Route route = new Route();
        route.setId(new FeedScopedId("0", "105"));
        route.setShortName("105");
        Stop stop1 = new Stop();
        stop1.setZoneId("1");
        stop1.setId(new FeedScopedId());
        Stop stop8 = new Stop();
        stop8.setZoneId("1");
        stop8.setId(new FeedScopedId());
        Stop stop11 = new Stop();
        stop11.setZoneId("1");
        stop11.setId(new FeedScopedId());
        List<TransitTripStage> tripStages = new ArrayList<>();

        /*

        Routes used in the tested itinerary:

        |  10 min   |    Travel time
        |<--------->|
        0       7   10   Arrive at stop time (minutes)
        |-------|---|
        |           |
        |   105     |    Mean of transport

         */
        tripStages.add(new TransitTripStage(route, stop1, 1, 0));
        tripStages.add(new TransitTripStage(route, stop8, 8, 10));
        tripStages.add(new TransitTripStage(route, stop11, 11, 5));
        assertEquals(11, generatedTicket.getTotalMinutesWhenValid(11, tripStages));

        route.getId().setId("L14");
        assertEquals(0, generatedTicket.getTotalMinutesWhenValid(11, tripStages));

        route.getId().setId("105");
        //Setting invalid id for route - ticket is not applicable
        route.getId().setAgencyId("ZTM");
        assertEquals(0, generatedTicket.getTotalMinutesWhenValid(11, tripStages));
    }

    @Test
    public void shouldReturnSingleWarsaw90MinutesTicketSingleFareVersion() {
        AvailableTransitTicketsGetter transitTicketGetter = new AvailableTransitTicketsGetter();
        Set<TransitTicket> tickets = transitTicketGetter.getFromFile("src/test/resources/tickets/warsaw/90minTicket_singleFare.json");

        assertEquals(1, tickets.size());
        TransitTicket generatedTicket = tickets.iterator().next();

        assertEquals(5, generatedTicket.getId());
        assertEquals(-1, generatedTicket.getMaxMinutes());
        assertEquals(-1, generatedTicket.getMaxDistance());
        assertEquals(1, generatedTicket.getMaxFares());
        assertEquals(0, generatedTicket.getStandardPrice().compareTo(BigDecimal.valueOf(7)));
        assertNull(generatedTicket.getAvailableFrom());
        assertNull(generatedTicket.getAvailableTo());

        RoutePattern ztmRoutePattern = generatedTicket.getRoutePattern("0");
        assertNotNull(ztmRoutePattern);

        Route route = new Route();
        route.setId(new FeedScopedId("0", "105"));
        route.setShortName("105");
        Stop stop1 = new Stop();
        stop1.setZoneId("1");
        stop1.setId(new FeedScopedId());
        Route route2 = new Route();
        route2.setId(new FeedScopedId("0", "4"));
        route2.setShortName("4");
        Stop stop8 = new Stop();
        stop8.setZoneId("1");
        stop8.setId(new FeedScopedId());
        Stop stop11 = new Stop();
        stop11.setZoneId("1");
        stop11.setId(new FeedScopedId());
        List<TransitTripStage> tripStages = new ArrayList<>();

        /*

        Routes used in the tested itinerary:

        |  10 min   |    Travel time
        |<--------->|
        0       7   10   Arrive at stop time (minutes)
        |-------|---|
        |       |   |
        |  105  | 4 |    Mean of transport

         */
        tripStages.add(new TransitTripStage(route, stop1, 1, 0));
        tripStages.add(new TransitTripStage(route, stop8, 8, 10));
        tripStages.add(new TransitTripStage(route, stop8, 8, 10));
        tripStages.add(new TransitTripStage(route, stop11, 11, 5));
        assertEquals(11, generatedTicket.getTotalMinutesWhenValid(11, tripStages));

        route.getId().setId("L14");
        assertEquals(0, generatedTicket.getTotalMinutesWhenValid(11, tripStages));

        route.getId().setId("105");
        tripStages = new ArrayList<>();
        tripStages.add(new TransitTripStage(route, stop1, 1, 0));
        tripStages.add(new TransitTripStage(route, stop8, 8, 10));
        tripStages.add(new TransitTripStage(route2, stop8, 8, 10));
        tripStages.add(new TransitTripStage(route2, stop11, 11, 5));
        assertEquals(4, generatedTicket.getTotalMinutesWhenValid(11, tripStages));

        //Setting invalid id for route - ticket is not applicable
        route2.getId().setAgencyId("ZTM");
        assertEquals(0, generatedTicket.getTotalMinutesWhenValid(11, tripStages));
    }

    @Test
    public void shouldReturnSingleWarsawDailyTicket() {
        AvailableTransitTicketsGetter transitTicketGetter = new AvailableTransitTicketsGetter();
        Set<TransitTicket> tickets = transitTicketGetter.getFromFile("src/test/resources/tickets/warsaw/24hTicketZone1.json");

        assertEquals(1, tickets.size());
        TransitTicket generatedTicket = tickets.iterator().next();

        assertEquals(6, generatedTicket.getId());
        assertEquals(1440, generatedTicket.getMaxMinutes());
        assertEquals(-1, generatedTicket.getMaxDistance());
        assertEquals(-1, generatedTicket.getMaxFares());
        assertEquals(0, generatedTicket.getStandardPrice().compareTo(BigDecimal.valueOf(15)));
        assertNull(generatedTicket.getAvailableFrom());
        assertNull(generatedTicket.getAvailableTo());

        RoutePattern ztmRoutePattern = generatedTicket.getRoutePattern("0");
        assertNotNull(ztmRoutePattern);

        Route route = new Route();
        route.setId(new FeedScopedId("0", "105"));
        route.setShortName("105");
        Stop stop1 = new Stop();
        stop1.setZoneId("1");
        stop1.setId(new FeedScopedId());
        Stop stop8 = new Stop();
        stop8.setZoneId("1");
        stop8.setId(new FeedScopedId());
        Stop stop11 = new Stop();
        stop11.setZoneId("1");
        stop11.setId(new FeedScopedId());
        List<TransitTripStage> tripStages = new ArrayList<>();

        /*

        Routes used in the tested itinerary:

        |  10 min   |    Travel time
        |<--------->|
        0       7   10   Arrive at stop time (minutes)
        |-------|---|
        |           |
        |   105     |    Mean of transport

         */
        tripStages.add(new TransitTripStage(route, stop1, 1, 0));
        tripStages.add(new TransitTripStage(route, stop8, 8, 10));
        tripStages.add(new TransitTripStage(route, stop11, 11, 5));
        assertEquals(11, generatedTicket.getTotalMinutesWhenValid(11, tripStages));

        stop1.setZoneId("2");
        stop8.setZoneId("1/2");
        //not valid for the entire trip due to zone id change
        assertEquals(4, generatedTicket.getTotalMinutesWhenValid(11, tripStages));

        //Setting invalid id for route - ticket is not applicable
        route.getId().setAgencyId("ZTM");
        assertEquals(0, generatedTicket.getTotalMinutesWhenValid(11, tripStages));
    }

}
