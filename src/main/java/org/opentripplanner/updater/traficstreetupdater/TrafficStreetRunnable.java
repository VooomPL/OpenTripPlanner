package org.opentripplanner.updater.traficstreetupdater;

import org.opentripplanner.graph_builder.module.time.EdgeLine;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.updater.GraphWriterRunnable;

import java.util.Map;

class TrafficStreetRunnable implements GraphWriterRunnable {

    private final Map<EdgeLine, Integer> map;

    public TrafficStreetRunnable(Map<EdgeLine, Integer> map) {
        this.map = map;
    }

    @Override
    public void run(Graph graph) {
        for (StreetEdge e : graph.getStreetEdges()) {
            e.setTemporarySpeedLimit(-1);
            EdgeLine el = new EdgeLine(e.getStartOsmNodeId(), e.getEndOsmNodeId());
            if (map.get(el) != null) {
                e.setTemporarySpeedLimit(map.get(el));
            }
        }
        graph.routerHealth.setTraffic(true);
    }
}
