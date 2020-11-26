package org.opentripplanner.updater.traficstreetupdater;

import com.fasterxml.jackson.databind.JsonNode;
import org.opentripplanner.graph_builder.linking.TemporaryStreetSplitter;
import org.opentripplanner.graph_builder.module.time.EdgeLine;
import org.opentripplanner.hasura_client.EdgeDataWithSpeedGetter;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.updater.GraphUpdaterManager;
import org.opentripplanner.updater.PollingGraphUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.HashMap;
import java.util.List;


public class TrafifcUpdater extends PollingGraphUpdater {
    private  Graph graph;
    GraphUpdaterManager graphUpdaterManager;
    private HashMap <EdgeLine,Integer> map = new HashMap<>();
    private static final Logger LOG = LoggerFactory.getLogger(TrafifcUpdater.class);
    private  String url;
    private  String pass;
    private final EdgeDataWithSpeedGetter edgeDataWithSpeedGetter= new EdgeDataWithSpeedGetter();
    private TemporaryStreetSplitter temporaryStreetSplitter;
      @Override
    protected void runPolling() {
        LOG.info("Polling trafic udates  from API");
        List updates = edgeDataWithSpeedGetter.postFromHasuraWithPassword(graph,url,pass);
        LOG.info("Got {} edeges with traffiv street", updates.size());
        for (Object e: updates)
        {
             map.put( new EdgeLine(((EdgeDataWithSpeed)e).getStartnodeid(),((EdgeDataWithSpeed)e).getEndnodeid()),((EdgeDataWithSpeed)e).getSpeed());
        }
          graphUpdaterManager.execute(new  TrafficStreetrrRunable(map));
    }

    @Override
    protected void configurePolling(Graph graph, JsonNode config) throws IllegalStateException {
        this.pollingPeriodSeconds = 10;


        this.url = System.getProperty("trfficApi");
        this.pass =System.getProperty("trfficApiPass");
        if (this.url == null) {
            throw new IllegalStateException("zzzzz ");
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
    public void configure(Graph graph, JsonNode config) throws Exception {
        configurePolling(graph, config);
    }
    @Override
    public void teardown() {

    }
}
