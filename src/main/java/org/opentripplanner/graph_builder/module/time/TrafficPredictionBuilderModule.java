package org.opentripplanner.graph_builder.module.time;

import org.opentripplanner.graph_builder.services.GraphBuilderModule;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.graph.Graph;

import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;


public class TrafficPredictionBuilderModule implements GraphBuilderModule {
    private final ClusterList clusterlist;

    public TrafficPredictionBuilderModule(File traficprediction) {
        this.clusterlist = new ClusterList(traficprediction);
        this.clusterlist.getclusters().sort(Comparator.naturalOrder());
    }

    boolean matchEdge(StreetEdge e, Cluster c) {
        for (EdgeData s : c.getedges()) {
            //LOG.error("mecch Edge  {} cluster {}",e.getId(),s.getclusterid());
            if (e.getStartOsmNodeId() == s.getstartnodeid() && e.getEndOsmNodeId() == s.getstartnodeid())
                return true;
        }

        return  false;
    }
    @Override
    public void buildGraph(Graph graph, HashMap<Class<?>, Object> extra) {
        Map<EdgeLine, Cluster> map = new HashMap<>();
        for (Cluster c : this.clusterlist.getclusters()) {
            for (EdgeData e : c.getedges())
                if (c.gettimetable() != null) {
                    map.put(new EdgeLine(e.getstartnodeid(), e.getendnodeid()), c);
                }
        }

        for (StreetEdge e : graph.getStreetEdges()) {
            Cluster cluster = map.get(new EdgeLine(e.getStartOsmNodeId(), e.getEndOsmNodeId()));
            if (cluster != null) {
                matchEdge(e, cluster);
                e.setTimes((cluster).gettimetableas());
                e.getTimes().sort(TimeTable::compareTo);
            }
        }
    }

    @Override
    public void checkInputs() {

    }

    @Override
    public void clearLocals() {
        clusterlist.setclusters(null);
    }
}
