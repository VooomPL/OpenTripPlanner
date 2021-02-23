package org.opentripplanner.graph_builder.module.distance_estimator;

import org.opentripplanner.graph_builder.services.GraphBuilderModule;
import org.opentripplanner.routing.graph.Estimators.GraphComponent.GraphComponentInfo;
import org.opentripplanner.routing.graph.Graph;

import java.util.HashMap;
import java.util.LinkedList;

public class GraphComponentModule implements GraphBuilderModule {
    @Override
    public void buildGraph(Graph graph, HashMap<Class<?>, Object> extra) {
        graph.graphComponentInfo = new GraphComponentInfo(graph, new LinkedList<>());
    }

    @Override
    public void checkInputs() {

    }
}
