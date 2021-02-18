package org.opentripplanner.routing.graph.Estimators;

import org.opentripplanner.routing.graph.DistanceEstimator;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;

public class ExactDijikstraEstimator extends DistanceEstimator {
    private Graph graph;

    public ExactDijikstraEstimator(Graph graph) {
        this.graph = graph;
    }

    @Override
    public double estimateDistanceInMeters(Graph graph, Vertex from, Vertex to) {
        return Landmark.calculateDistances(from, graph, to)[to.getIndex()];
    }
}
