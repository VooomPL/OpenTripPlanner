package org.opentripplanner.updater.traficstreetupdater;

import com.fasterxml.jackson.databind.JsonNode;
import org.opentripplanner.graph_builder.module.time.EdgeLine;
import org.opentripplanner.hasura_client.BikeStationsGetter;
import org.opentripplanner.hasura_client.EdgeDataWithSpeedGetter;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleDescription;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.updater.GraphUpdaterManager;
import org.opentripplanner.updater.PollingGraphUpdater;
import org.opentripplanner.updater.bike_park.BikeParkUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.HashMap;
import java.util.List;


public class TrafifcUpdater extends PollingGraphUpdater {
    private  Graph graph;
    GraphUpdaterManager graphUpdaterManager;
    private HashMap <EdgeLine,Integer> map;
    private static final Logger LOG = LoggerFactory.getLogger(TrafifcUpdater.class);
    private String url;
    private  String pass;
    private final EdgeDataWithSpeedGetter edgeDataWithSpeedGetter= new EdgeDataWithSpeedGetter();
      @Override
    protected void runPolling() {
        LOG.info("Polling trafic from API");
          List<EdgeDataWithSpeed> updates = edgeDataWithSpeedGetter.getFromHasuraWithPassword(graph,url,pass);
        LOG.info("Got {} trafic possible to place on a map", updates.size());
        graphUpdaterManager.execute(new TrafficStreetrrRunable(map));
    }

    @Override
    protected void configurePolling(Graph graph, JsonNode config) throws IllegalStateException {
        this.pollingPeriodSeconds = 60;
       this.url = System.getProperty("sharedVehiclesApi");
        if (this.url == null) {
            throw new IllegalStateException("Please provide program parameter `--sharedVehiclesApi <URL>`");
        }


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
