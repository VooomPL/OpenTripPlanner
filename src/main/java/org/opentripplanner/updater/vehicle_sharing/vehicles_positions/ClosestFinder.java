package org.opentripplanner.updater.vehicle_sharing.vehicles_positions;

import org.opentripplanner.common.geometry.SphericalDistanceLibrary;
import org.opentripplanner.common.pqueue.BinHeap;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;

import java.util.HashMap;
import java.util.List;

public class ClosestFinder {
    private static class State {
        Vertex v;
        double p;
        Vertex beginingVertex;

        public State(Vertex v, double p, Vertex beginingVertex) {
            this.v = v;
            this.p = p;
            this.beginingVertex = beginingVertex;

        }
    }

    public interface SaveBestResultInterface {
        void saveBestResult(Vertex vertex, Double d);
    }

    public static void findAllClosest(Graph graph, List<Vertex> startVertices, SaveBestResultInterface visitor) {
        HashMap<Vertex, Double> bestFoundDistance = new HashMap<>();
        BinHeap<State> priorityQueue = new BinHeap<>();
//        Dijikstra starts in all startVertices
        for (Vertex v : startVertices) {
            priorityQueue.insert(new State(v, 0, v), 0);
            bestFoundDistance.put(v, 0D);
        }

        while (!priorityQueue.empty()) {
            State s = priorityQueue.extract_min();
            if (s.p > bestFoundDistance.getOrDefault(s.v, Double.MAX_VALUE))
                continue;

            visitor.saveBestResult(s.v, s.p);

            for (Edge edge : s.v.getOutgoing()) {
                Vertex v2 = edge.getToVertex();
                double edgeLength = SphericalDistanceLibrary.fastDistance(s.v.getLat(), s.v.getLon(), v2.getLat(), v2.getLon());
                if (s.p + edgeLength < bestFoundDistance.getOrDefault(v2, Double.MAX_VALUE)) {
                    bestFoundDistance.put(v2, s.p + edgeLength);
                    priorityQueue.insert(new State(v2, s.p + edgeLength, s.beginingVertex), s.p + edgeLength);
                }

            }
        }

    }
}
