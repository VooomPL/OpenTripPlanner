package org.opentripplanner.routing.graph.Estimators.GraphComponent;

import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

public class GraphComponentInfo implements Serializable {
    Map<Vertex, GraphComponent> components;
    private static final Logger LOG = LoggerFactory.getLogger(GraphComponentInfo.class);


    public GraphComponent getGraphComponent(Vertex vertex) {
        int count = 0;
        if (components.get(vertex) != null) {
            return components.get(vertex);
        }

//        TODO big debug of whole class is needed.
//        All vertices that were found during this search.
        Set<Vertex> reachableVertices = new HashSet<>();
        reachableVertices.add(vertex);
//       Vertices which were found but not processed.
        List<Vertex> verticesToProcess = new LinkedList<>();
        verticesToProcess.add(vertex);

        while (!verticesToProcess.isEmpty()) {
            count++;
            vertex = verticesToProcess.get(0);
            verticesToProcess.remove(0);
            if (components.get(vertex) != null) {
                if (count > 2)

                    return components.get(vertex);
            }
            List<Vertex> incidentVertices = vertex.getAllIncidentVertices();


            for (Vertex neighbourVertex : incidentVertices) {
                if (!reachableVertices.contains(neighbourVertex)) {
                    if (components.get(neighbourVertex) != null) {

                        return components.get(neighbourVertex);
                    }
                    reachableVertices.add(neighbourVertex);
                    verticesToProcess.add(neighbourVertex);
                }
            }
        }

//        Return dummy GraphComponent
        return new GraphComponent(-1, 1);
    }

    public GraphComponentInfo(Graph graph, List<Class> forbiddenEdges) {
        components = new HashMap<>();
        int numberOfComponents = 0;
        Set<Vertex> visitedVertices = new HashSet<>();
        for (Vertex vertex : graph.getVertices()) {
            if (!visitedVertices.contains(vertex)) {
                Set<Vertex> verticesInComponent = visitComponent(vertex, forbiddenEdges);
                visitedVertices.addAll(verticesInComponent);
                GraphComponent graphComponent = new GraphComponent(numberOfComponents, verticesInComponent.size());
                numberOfComponents++;

                for (Vertex vertex1 : verticesInComponent) {
                    components.put(vertex1, graphComponent);
                }
            }
        }
        LOG.info("Found " + numberOfComponents + " components in final graph");
    }

    //  Visits all nodes reachable from vertex via
    private Set<Vertex> visitComponent(Vertex sourceVertex, List<Class> forbiddenEdges) {
        Set<Vertex> reachableVertices = new HashSet<>();
        reachableVertices.add(sourceVertex);
        List<Vertex> verticesToProcess = new LinkedList<>();
        verticesToProcess.add(sourceVertex);

        while (!verticesToProcess.isEmpty()) {
            sourceVertex = verticesToProcess.get(0);
            verticesToProcess.remove(0);

            List<Vertex> incidentVertices = new LinkedList<>();

            for (Edge edge : sourceVertex.getAllIncidentEdges()) {
                if (!forbiddenEdges.contains(edge.getClass())) {
                    incidentVertices.add(edge.getToVertex());
                    incidentVertices.add(edge.getFromVertex());
                }
            }

            for (Vertex vertex : incidentVertices) {
                if (!reachableVertices.contains(vertex)) {
                    reachableVertices.add(vertex);
                    verticesToProcess.add(vertex);
                }
            }
        }

        return reachableVertices;
    }

}
