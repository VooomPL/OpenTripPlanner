package org.opentripplanner.updater.transit.ticket;

import com.fasterxml.jackson.databind.JsonNode;
import org.opentripplanner.pricing.transit.ticket.TransitTicket;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.updater.GraphUpdaterManager;
import org.opentripplanner.updater.PollingGraphUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class AvailableTransitTicketsUpdater extends PollingGraphUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(AvailableTransitTicketsUpdater.class);

    private final AvailableTransitTicketsGetter ticketsGetter = new AvailableTransitTicketsGetter();

    private GraphUpdaterManager graphUpdaterManager;

    private Graph graph;

    private String ticketDefinitionsFileName;

    @Override
    protected void runPolling() {
        LOG.info("Polling available tickets from file");
        Set<TransitTicket> availableTickets = ticketsGetter.getFromFile(this.ticketDefinitionsFileName);
        if (!availableTickets.isEmpty()) {
            availableTickets.forEach(transitTicket -> graph.getAvailableTransitTickets().add(transitTicket));
        } else {
            LOG.warn("Couldn't read available tickets from file");
        }
    }

    @Override
    protected void configurePolling(Graph graph, JsonNode config) {
        this.pollingPeriodSeconds = 10800;
        this.ticketDefinitionsFileName = System.getProperty("ticketsDefinitionsFile");
    }

    @Override
    public void configure(Graph graph, JsonNode config) {
        configurePolling(graph, config);
        type = "Available transit tickets";
    }

    @Override
    public void setGraphUpdaterManager(GraphUpdaterManager updaterManager) {
        this.graphUpdaterManager = updaterManager;
    }

    @Override
    public void setup(Graph graph) {
        this.graph = graph;
    }

    @Override
    public void teardown() {

    }

}
