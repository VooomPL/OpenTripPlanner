package org.opentripplanner.routing.algorithm.strategies.connection_matrix_heuristic;

import org.opentripplanner.routing.algorithm.strategies.RemainingWeightHeuristic;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.State;

import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Queue;

// TODO VMP-250 finish implementation, add tests
public class ConnectionMatrixRWH implements RemainingWeightHeuristic {

    private static final float INITIAL_WEIGHT = 180.0f; // TODO read from data

    private ConnectionMatrixHeuristicData data;

    private boolean[][] visited;

    private Queue<PointWithWeight> queue;

    private Point destination;

    @Override
    public void initialize(RoutingRequest options, long abortTime) {
        this.data = options.rctx.graph.connectionMatrixHeuristicData;
        visited = new boolean[data.getBoundaries().getHeight()][data.getBoundaries().getWidth()];
        queue = new PriorityQueue<>(data.getBoundaries().getHeight() * data.getBoundaries().getWidth());
        destination = data.mapToPoint(options.to); // TODO outside boundaries
    }

    @Override
    public double estimateRemainingWeight(State s) {
        Arrays.stream(visited).forEach(array -> Arrays.fill(array, false));
        queue.clear();

        Point source = data.mapToPoint(s.getVertex());
        if (!data.getBoundaries().contains(source)) {
            return 0.; // TODO euclidean
        }
        queue.add(new PointWithWeight(data.mapToPoint(s.getVertex()), INITIAL_WEIGHT));

        while (!queue.isEmpty()) {
            PointWithWeight current = queue.poll();
            if (current.getPoint().equals(destination)) {
                return current.getWeight();
            }
            if (visited[current.getPoint().getI()][current.getPoint().getJ()]) {
                continue;
            }
            visited[current.getPoint().getI()][current.getPoint().getJ()] = true;
            data.getNeighbors(current.getPoint())
                    .filter(neighbor -> !visited[neighbor.getPoint().getI()][neighbor.getPoint().getJ()])
                    .map(neighbor -> new PointWithWeight(neighbor.getPoint(),
                            current.getWeight() + data.getCost(current.getPoint(), neighbor.getDirection())))
                    .filter(neighbor -> !Float.isNaN(neighbor.getWeight()))
                    .forEach(queue::add);
        }

        return 0.; // TODO euclidean / error
    }

    @Override
    public void reset() {
    }

    @Override
    public void doSomeWork() {
    }
}
