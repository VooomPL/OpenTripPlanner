package org.opentripplanner.updater.traficstreetupdater;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.opentripplanner.graph_builder.module.time.EdgeLine;
import org.opentripplanner.hasura_client.EdgeDataWithSpeedGetter;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.updater.GraphUpdaterManager;
import org.opentripplanner.updater.PollingGraphUpdater;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;

@Slf4j
public class TrafficUpdater extends PollingGraphUpdater {

    private GraphUpdaterManager graphUpdaterManager;
    private final EdgeDataWithSpeedGetter edgeDataWithSpeedGetter = new EdgeDataWithSpeedGetter();

    private Graph graph;
    private String url;
    private String pass;

    @Override
    protected void runPolling() {
        log.info("Polling traffic updates from API");
        List<EdgeDataWithSpeed> data = edgeDataWithSpeedGetter.postFromHasuraWithPassword(graph, url, pass);
        log.info("Got {} edges with traffic update", data.size());
        Map<EdgeLine, Integer> update = data.stream().collect(
                toMap(edge -> new EdgeLine(edge.getStartnodeid(), edge.getEndnodeid()), EdgeDataWithSpeed::getSpeed));
        graphUpdaterManager.execute(new TrafficStreetRunnable(update));
    }

    @Override
    protected void configurePolling(Graph graph, JsonNode config) throws IllegalStateException {
        this.pollingPeriodSeconds = 10;
        this.url = System.getProperty("trafficPredictionApi");
        // TODO Adam Wiktor use only `getenv` after VMP-182
        this.pass = Optional.ofNullable(System.getenv("TRAFFIC_PREDICTION_API_PASS"))
                .orElseGet(() -> System.getProperty("trafficPredictionApiPass"));
        if (this.url == null) {
            throw new IllegalStateException("Traffic prediction api is not set, traffic updater will not work");
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
