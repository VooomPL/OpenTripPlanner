package org.opentripplanner.updater.transit.ticket;

import org.junit.Test;
import org.opentripplanner.routing.graph.Graph;

import static org.junit.Assert.assertEquals;

public class AvailableTransitTicketUpdaterTest {

    @Test
    public void shouldAdd7TicketsToGraph() {
        System.setProperty("ticketsDefinitionsFile", "src/test/resources/tickets/warsaw/availableTickets.json");
        Graph graph = new Graph();

        AvailableTransitTicketsUpdater updater = new AvailableTransitTicketsUpdater();
        updater.setup(graph);
        updater.configure(graph, null);
        updater.runPolling();

        assertEquals(7, graph.getAvailableTransitTickets().size());
    }

    @Test
    public void shouldNotAddAnyTicketToGraph() {
        Graph graph = new Graph();
        System.clearProperty("ticketsDefinitionsFile");

        AvailableTransitTicketsUpdater updater = new AvailableTransitTicketsUpdater();
        updater.setup(graph);
        updater.configure(graph, null);
        updater.runPolling();

        assertEquals(0, graph.getAvailableTransitTickets().size());
    }
}
