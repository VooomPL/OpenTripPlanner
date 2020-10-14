package org.opentripplanner.updater.traficstreetupdater;

import com.fasterxml.jackson.databind.JsonNode;
import org.opentripplanner.graph_builder.linking.TemporaryStreetSplitter;
import org.opentripplanner.graph_builder.module.time.EdgeLine;
import org.opentripplanner.graph_builder.module.time.TimeTable;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.updater.GraphUpdaterManager;
import org.opentripplanner.updater.PollingGraphUpdater;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;


public class traficupdater  extends PollingGraphUpdater {
    private  Graph graph;
    GraphUpdaterManager graphUpdaterManager;
    private HashMap <EdgeLine,Integer> mao;
      @Override
    protected void runPolling() {
        //LOG.info("Polling trafic from API");
        ArrayList<EdgeDtaWithSpeed> Data ;
        //LOG.info("Got {} vehicles possible to place on a map", vehicles.size());
        graphUpdaterManager.execute(new TraficStreetrrRunable(mao));
    }

    @Override
    protected void configurePolling(Graph graph, JsonNode config) throws IllegalStateException {
        this.pollingPeriodSeconds = 60;
       /*this.url = System.getProperty("sharedVehiclesApi");
        if (this.url == null) {
            throw new IllegalStateException("Please provide program parameter `--sharedVehiclesApi <URL>`");*/
        }


    @Override
    public void configure(Graph graph, JsonNode config) throws Exception {
        configurePolling(graph, config);
        type = "Shared Vehicles";
    }

    @Override
    public void setGraphUpdaterManager(GraphUpdaterManager updaterManager) {
        this.graphUpdaterManager = updaterManager;
    }

    @Override
    public void setup(Graph graph) throws Exception {
        this.graph = graph;

    }

    @Override
    public void teardown() {

    }
}
