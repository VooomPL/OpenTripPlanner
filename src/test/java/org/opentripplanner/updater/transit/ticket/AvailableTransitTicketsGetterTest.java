package org.opentripplanner.updater.transit.ticket;

import org.junit.Test;
import org.opentripplanner.model.FeedScopedId;
import org.opentripplanner.model.Route;
import org.opentripplanner.pricing.transit.ticket.TransitTicket;
import org.opentripplanner.pricing.transit.ticket.pattern.FareSwitchPattern;
import org.opentripplanner.pricing.transit.ticket.pattern.RoutePattern;

import java.math.BigDecimal;
import java.time.Month;
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

        assertTrue(tickets.size() == 1);
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

        //TODO: test StopPattern
    }

    //TODO: check when some values are not present in json!!

    //TODO: test when date is not properly formatted

    //TODO: test for Warsaw and Tristar

}
