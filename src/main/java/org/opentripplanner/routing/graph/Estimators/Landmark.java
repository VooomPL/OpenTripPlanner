package org.opentripplanner.routing.graph.Estimators;

import org.opentripplanner.common.geometry.SphericalDistanceLibrary;
import org.opentripplanner.common.pqueue.BinHeap;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Math.abs;

public class Landmark implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(Landmark.class);
    private int failedAproximations = 0;
    private Vertex chosenVertex;
    private double distances[];
    private int distanceSize;

    public static double[] calculateDistances(Vertex vertex, Graph graph, Vertex destination) {
        double[] distances = new double[Vertex.getMaxIndex() + 1];
        BinHeap<Vertex> priorityQueue = new BinHeap<>();
        priorityQueue.insert(vertex, 0);
        int vertexes = 0;
        for (int i = 0; i < Vertex.getMaxIndex(); i++)
            distances[i] = Double.MAX_VALUE;
        distances[vertex.getIndex()] = 0;

        while (!priorityQueue.empty()) {
            Vertex currentVertex = priorityQueue.peek_min();
            double currentDistance = distances[currentVertex.getIndex()];
//            Check if vertex wasn't processed in the past
            if (priorityQueue.peek_min_key() > currentDistance) {
                priorityQueue.extract_min();
                continue;
            }
            if (currentVertex.equals(destination))
                break;
            vertexes++;
            priorityQueue.extract_min();

            List<Edge> allEdges = new LinkedList<>();
            allEdges.addAll(currentVertex.getOutgoing());
            allEdges.addAll(currentVertex.getIncoming());

            for (Edge edge : allEdges) {
                if (true) {
//                if (!(edge instanceof TransitBoardAlight) && !(edge instanceof HopEdge) && !(edge instanceof SimpleTransfer)) {
                    Vertex v2 = edge.getToVertex();
                    double edgeLength;
                    if (edge.getDistanceInMeters() != 0)
                        edgeLength = edge.getDistanceInMeters();
                    else
                        edgeLength = SphericalDistanceLibrary.fastDistance(currentVertex.getLat(), currentVertex.getLon(), v2.getLat(), v2.getLon());

                    if (currentDistance + edgeLength < distances[v2.getIndex()]) {
                        distances[v2.getIndex()] = currentDistance + edgeLength;
                        priorityQueue.insert(v2, currentDistance + edgeLength);
                    }

                    v2 = edge.getFromVertex();
                    if (edge.getDistanceInMeters() != 0)
                        edgeLength = edge.getDistanceInMeters();
                    else
                        edgeLength = SphericalDistanceLibrary.fastDistance(currentVertex.getLat(), currentVertex.getLon(), v2.getLat(), v2.getLon());

                    if (currentDistance + edgeLength < distances[v2.getIndex()]) {
                        distances[v2.getIndex()] = currentDistance + edgeLength;
                        priorityQueue.insert(v2, currentDistance + edgeLength);
                    }
                }
            }
        }
        LOG.info("Calculated distances for " + vertexes + " out of " + graph.getVertices().size() + " vertices");

        return distances;
    }

    public Landmark(Vertex vertex, Graph graph) {
        LOG.info("Creating landmark from " + vertex.toString());
        this.chosenVertex = vertex;
        distances = Landmark.calculateDistances(vertex, graph, null);
        distanceSize = Vertex.getMaxIndex() + 1;

        Vertex unvisitedVertex = graph.getVertices().stream().filter(vertex1 -> distances[vertex1.getIndex()] == Double.MAX_VALUE).collect(Collectors.toList()).get(0);


    }


    public double distanceFromLandmark(Graph graph, Vertex v, boolean checkComponent) {
        if (checkComponent) {
            if (!graph.graphComponentInfo.getGraphComponent(v).equals(graph.graphComponentInfo.getGraphComponent(chosenVertex))) {
                LOG.info("Vertex not in same component as Landmark, returning euclidean estimation");
                return SphericalDistanceLibrary.distance(v.getLat(), v.getLon(), chosenVertex.getLat(), chosenVertex.getLon());
            }
        }

        if (v.getIndex() < distanceSize && distances[v.getIndex()] != Double.MAX_VALUE)
            return distances[v.getIndex()];

        Set<Vertex> visited = new HashSet<>();
        List<Vertex> vertices = new LinkedList<>();

        vertices.add(v);
        visited.add(v);
        int visitedCount = 0;
        while (!vertices.isEmpty()) {
            if (visitedCount > 40)
                break;
            visitedCount++;
            Vertex currentVertex = vertices.get(0);
            vertices.remove(0);

            for (Edge edge : currentVertex.getOutgoing()) {
                if (!visited.contains(edge.getToVertex())) {
                    if (edge.getToVertex().getIndex() < distanceSize && distances[edge.getToVertex().getIndex()] < Double.MAX_VALUE) {
                        return distances[edge.getToVertex().getIndex()];
                    }
                    visited.add(edge.getToVertex());

                    vertices.add(edge.getToVertex());
                }
            }
            for (Edge edge : currentVertex.getIncoming()) {
                if (!visited.contains(edge.getFromVertex())) {
                    if (edge.getFromVertex().getIndex() < distanceSize && distances[edge.getFromVertex().getIndex()] < Double.MAX_VALUE) {
                        return distances[edge.getFromVertex().getIndex()];
                    }
                    visited.add(edge.getFromVertex());

                    vertices.add(edge.getFromVertex());
                }
            }

        }
        failedAproximations++;
        if (failedAproximations % 10 == 1)
            LOG.warn("Connection do landmark spt not found, returning euclidean approximation for the " + failedAproximations + "th time");
        return SphericalDistanceLibrary.distance(v.getLat(), v.getLon(), chosenVertex.getLat(), chosenVertex.getLon());

    }

    public double estimateDistanceInMeters(Graph graph, Vertex from, Vertex to) {
        return abs(distanceFromLandmark(graph, from, false) - distanceFromLandmark(graph, to, false));
    }

    public Vertex getChosenVertex() {
        return chosenVertex;
    }
}
