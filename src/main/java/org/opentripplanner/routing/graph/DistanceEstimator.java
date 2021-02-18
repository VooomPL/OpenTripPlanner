package org.opentripplanner.routing.graph;

import java.io.Serializable;

public abstract class DistanceEstimator implements Serializable {
    public abstract double estimateDistanceInMeters(Graph graph, Vertex from, Vertex to);
}
