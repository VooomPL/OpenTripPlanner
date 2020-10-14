package org.opentripplanner.updater.traficstreetupdater;

import org.opentripplanner.graph_builder.module.time.EdgeLine;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.updater.GraphWriterRunnable;

import java.util.HashMap;

public class TraficStreetrrRunable implements GraphWriterRunnable {
    private HashMap<EdgeLine, Integer> map ;

    public TraficStreetrrRunable(HashMap<EdgeLine, Integer> map) {
        this.map = map;
    }

    @Override
    public void run(Graph graph) {
        for (StreetEdge e : graph.getStreetEdges()){
            if( map.get( new EdgeLine(e.getStartOsmNodeId(),e.getEndOsmNodeId()))!=null){
            e.setTemporarySpeedLimit((int)map.get( new EdgeLine(e.getStartOsmNodeId(),e.getEndOsmNodeId())));
            if (e.getTemporarySpeedLimit()==0)
                e.setClosed(true);
            }
        }

    }
}
