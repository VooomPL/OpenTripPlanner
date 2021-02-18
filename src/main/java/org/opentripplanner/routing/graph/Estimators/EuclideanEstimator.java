package org.opentripplanner.routing.graph.Estimators;

import org.opentripplanner.common.geometry.SphericalDistanceLibrary;
import org.opentripplanner.routing.graph.DistanceEstimator;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;

public class EuclideanEstimator extends DistanceEstimator {
    @Override
    public double estimateDistanceInMeters(Graph graph, Vertex from, Vertex to) {
        return SphericalDistanceLibrary.distance(from.getLat(), from.getLon(), to.getLat(), to.getLon());
    }

}
