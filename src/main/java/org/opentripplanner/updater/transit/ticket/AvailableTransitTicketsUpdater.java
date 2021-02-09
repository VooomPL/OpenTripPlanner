package org.opentripplanner.updater.transit.ticket;

import com.fasterxml.jackson.databind.JsonNode;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.updater.GraphUpdaterManager;
import org.opentripplanner.updater.PollingGraphUpdater;

public class AvailableTransitTicketsUpdater extends PollingGraphUpdater {

    @Override
    protected void runPolling() throws Exception {

    }

    @Override
    protected void configurePolling(Graph graph, JsonNode config) throws Exception {

    }

    @Override
    public void setGraphUpdaterManager(GraphUpdaterManager updaterManager) {

    }

    @Override
    public void setup(Graph graph) throws Exception {

    }

    @Override
    public void teardown() {

    }
}
