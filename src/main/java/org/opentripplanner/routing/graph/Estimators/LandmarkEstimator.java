package org.opentripplanner.routing.graph.Estimators;

import javafx.util.Pair;
import org.opentripplanner.routing.graph.DistanceEstimator;
import org.opentripplanner.routing.graph.Estimators.GraphComponent.GraphComponent;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.vertextype.OsmVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static java.lang.Math.pow;

public class LandmarkEstimator extends DistanceEstimator {
    private static final Logger LOG = LoggerFactory.getLogger(LandmarkEstimator.class);
    private double[] distances;

    @Override
    public double estimateDistanceInMeters(Graph graph, Vertex from, Vertex to) {
        return landmarks
                .stream()
                .map(landmark -> landmark.estimateDistanceInMeters(graph, from, to))
                .max(Double::compareTo)
                .get();
    }

    private List<Landmark> landmarks;

    public LandmarkEstimator(Graph graph, int numberOfLandmarks) {

        LOG.info("Creating " + numberOfLandmarks + " landmarks");
        graph.rebuildVertexAndEdgeIndices();

        distances = new double[Vertex.getMaxIndex()];

        landmarks = chooseVerticesForLandmarks(graph, numberOfLandmarks);
    }

    private double sumOfDistances(Vertex vertex, List<Landmark> prevLandmarks) {
        return distances[vertex.getIndex()];
//        return prevLandmarks.stream().map(l -> l.distanceFromLandmark(vertex)).mapToDouble(Double::doubleValue).sum();
    }

    private Vertex chooseRandomVertex(Graph graph, List<Landmark> prevLandmarks) {
        int ind = new Random().nextInt(Vertex.getMaxIndex());
        while (graph.getVertexById(ind) == null) {
            ind++;
        }
        LOG.info("Choose " + ind + " vertex as  " + prevLandmarks.size() + "th vertex at random");

        return graph.getVertexById(ind);
    }

    private boolean isVertexPlausibleCandidateForLandmark(Vertex vertex, Graph graph, List<Landmark> prevLandmarks, GraphComponent biggestComponent) {
        boolean sameComponent = graph.graphComponentInfo.getGraphComponent(vertex).equals(biggestComponent);
        boolean osmVertex = vertex instanceof OsmVertex;
        return sameComponent && osmVertex;
    }

    private Vertex chooseNextVertex(Graph graph, List<Landmark> prevLandmarks, GraphComponent biggestComponent) {
        Vertex furthestVertex = (Vertex) graph.getVertices()
                .stream()
                .filter(vertex -> isVertexPlausibleCandidateForLandmark(vertex, graph, prevLandmarks, biggestComponent))
                .map(landmarkCandidate -> new Pair(landmarkCandidate, sumOfDistances(landmarkCandidate, prevLandmarks)))
                .max(Comparator.comparing(v -> ((Double) v.getValue())))
                .get()
                .getKey();

//        if(prevLandmarks.stream().map(Landmark::getChosenVertex).collect(toList()).contains(furthestVertex)){
//            LOG.info(furthestVertex.getIndex()+" is already a landmark");
//            return chooseRandomVertex(graph,prevLandmarks);
//        }
        return furthestVertex;
    }

    private List<Landmark> chooseVerticesForLandmarks(Graph graph, int numberOfLandmarks) {
        GraphComponent biggestComponent = new GraphComponent(0, 0);
        Vertex firstVertex = graph.getVertexById(0);

        for (Vertex vertex : graph.getVertices()) {
            GraphComponent graphComponent = graph.graphComponentInfo.getGraphComponent(vertex);
            if (graphComponent.getSize() > biggestComponent.getSize()) {
                biggestComponent = graphComponent;
                firstVertex = vertex;
            }
        }
        Landmark firstLandmark = new Landmark(firstVertex, graph);
        for (Vertex vertex : graph.getVertices()) {
            distances[vertex.getIndex()] = firstLandmark.distanceFromLandmark(graph, vertex, true);
        }
        firstVertex = chooseNextVertex(graph, Collections.singletonList(firstLandmark), biggestComponent);
        for (Vertex vertex : graph.getVertices()) {
            distances[vertex.getIndex()] = 0;
        }
        firstLandmark = new Landmark(firstVertex, graph);

        List<Landmark> landmarks = new LinkedList<>();
        landmarks.add(firstLandmark);

        while (landmarks.size() < numberOfLandmarks) {
            Vertex nextVertex = chooseNextVertex(graph, landmarks, biggestComponent);
            Landmark nextLandmark = new Landmark(nextVertex, graph);
            for (Vertex vertex : graph.getVertices()) {
                distances[vertex.getIndex()] += pow(nextLandmark.distanceFromLandmark(graph, vertex, true), 0.1);
            }
            landmarks.add(nextLandmark);
        }
        return landmarks;
    }

    public List<Landmark> getLandmarks() {
        return landmarks;
    }
}
