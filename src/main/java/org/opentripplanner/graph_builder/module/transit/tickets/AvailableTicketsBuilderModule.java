package org.opentripplanner.graph_builder.module.transit.tickets;

import lombok.AllArgsConstructor;
import org.opentripplanner.graph_builder.services.GraphBuilderModule;
import org.opentripplanner.pricing.transit.ticket.TransitTicket;
import org.opentripplanner.routing.graph.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

@AllArgsConstructor
public class AvailableTicketsBuilderModule implements GraphBuilderModule {

    private static final Logger LOG = LoggerFactory.getLogger(AvailableTicketsBuilderModule.class);

    private final AvailableTransitTicketsReader ticketsGetter = new AvailableTransitTicketsReader();

    private final File availableTransitTicket;

    @Override
    public void buildGraph(Graph graph, HashMap<Class<?>, Object> extra) {
        LOG.info("Reading available tickets from file");
        Set<TransitTicket> availableTickets = ticketsGetter.getFromFile(availableTransitTicket);
        if (availableTickets.isEmpty()) {
            LOG.warn("Couldn't read available tickets from file - file empty or not found");
        }
        LOG.info("Read {} transit ticket types", availableTickets.size());
        graph.getAvailableTransitTickets().clear();
        availableTickets.forEach(transitTicket -> graph.getAvailableTransitTickets().add(transitTicket));
    }

    @Override
    public void checkInputs() {

    }
}
