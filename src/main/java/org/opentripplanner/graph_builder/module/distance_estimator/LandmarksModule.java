package org.opentripplanner.graph_builder.module.distance_estimator;

import org.opentripplanner.graph_builder.services.GraphBuilderModule;
import org.opentripplanner.routing.graph.Estimators.EuclideanEstimator;
import org.opentripplanner.routing.graph.Estimators.ExactDijikstraEstimator;
import org.opentripplanner.routing.graph.Estimators.LandmarkEstimator;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Random;

public class LandmarksModule implements GraphBuilderModule {
    private static final Logger LOG = LoggerFactory.getLogger(LandmarksModule.class);

    @Override
    public void buildGraph(Graph graph, HashMap<Class<?>, Object> extra) {
        graph.setDistanceEstimator(new LandmarkEstimator(graph, 15));
//        for(Landmark landmark:((LandmarkEstimator) graph.getDistanceEstimator()).getLandmarks())
        benchmarkEstimator(graph);

    }

    private void benchmarkEstimator(Graph graph) {
        Random rand = new Random();
        for (int i = 0; i < 10000; i++) {
            int randomInd = rand.nextInt(graph.getVertices().size());
            Vertex randomVertex1 = graph.getVertexById(0);
            for (Vertex vertex : graph.getVertices()) {
                randomInd--;
                if (randomInd <= 0)
                    break;
                randomVertex1 = vertex;
            }

            randomInd = rand.nextInt(graph.getVertices().size());
            Vertex randomVertex2 = graph.getVertexById(0);
            for (Vertex vertex : graph.getVertices()) {
                randomInd--;
                if (randomInd <= 0)
                    break;
                randomVertex2 = vertex;
            }
            double landmarkDistance = graph.getDistanceEstimator().estimateDistanceInMeters(graph, randomVertex1, randomVertex2);
            double euclideanDistance = new EuclideanEstimator().estimateDistanceInMeters(graph, randomVertex1, randomVertex2);
            double exactDistance = new ExactDijikstraEstimator(graph).estimateDistanceInMeters(graph, randomVertex1, randomVertex2);
            LOG.info("landmark " + landmarkDistance + " euclidean " + euclideanDistance + " exact " + exactDistance + "  " +
                    (landmarkDistance / exactDistance) + " " + (euclideanDistance / exactDistance) + " " + (landmarkDistance / euclideanDistance));


        }
    }

    @Override
    public void checkInputs() {

    }
}
