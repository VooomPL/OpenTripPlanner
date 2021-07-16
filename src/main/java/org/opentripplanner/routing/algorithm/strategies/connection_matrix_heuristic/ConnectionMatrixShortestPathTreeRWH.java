package org.opentripplanner.routing.algorithm.strategies.connection_matrix_heuristic;

import org.opentripplanner.routing.algorithm.strategies.RemainingWeightHeuristic;
import org.opentripplanner.routing.algorithm.strategies.SimpleEuclideanRWH;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.State;

import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Queue;

public class ConnectionMatrixShortestPathTreeRWH implements RemainingWeightHeuristic {

    private final RemainingWeightHeuristic fallback = new SimpleEuclideanRWH();

    private ConnectionMatrixHeuristicData data;

    private float[][] estimation;

    private boolean[][] visited;

    private Queue<PointWithWeight> queue;

    private Point destination;

    @Override
    public void initialize(RoutingRequest options, long abortTime) {
        fallback.initialize(options, abortTime);
        this.data = options.rctx.graph.connectionMatrixHeuristicData;
        estimation = new float[data.getBoundaries().getHeight()][data.getBoundaries().getWidth()];
        destination = data.mapToPoint(options.rctx.target);
        spt();
    }

    @Override
    public double estimateRemainingWeight(State s) {
        Point source = data.mapToPoint(s.getVertex());
        if (!data.getBoundaries().contains(source) || !data.getBoundaries().contains(destination)) {
            return fallback.estimateRemainingWeight(s);
        }
        return 0.;
//        return estimation[source.getI()][source.getJ()];
    }

    private void spt() {
        visited = new boolean[data.getBoundaries().getHeight()][data.getBoundaries().getWidth()];
        Arrays.stream(visited).forEach(array -> Arrays.fill(array, false));
        queue = new PriorityQueue<>(data.getBoundaries().getHeight() * data.getBoundaries().getWidth());
        queue.add(new PointWithWeight(destination, data.getInitialWeight()));

        while (!queue.isEmpty()) {
            iterate();
        }
    }

    private void iterate() {
        PointWithWeight current = queue.poll();
        if (visited[current.getPoint().getI()][current.getPoint().getJ()]) {
            return;
        }
        visited[current.getPoint().getI()][current.getPoint().getJ()] = true;
        estimation[current.getPoint().getI()][current.getPoint().getJ()] = current.getPoint().equals(destination) ? 0.f : current.getWeight();
        addNeighbors(current);
    }

    private void addNeighbors(PointWithWeight current) {
        data.getNeighbors(current.getPoint())
                .filter(neighbor -> !visited[neighbor.getPoint().getI()][neighbor.getPoint().getJ()])
                .map(neighbor -> new PointWithWeight(neighbor.getPoint(), current.getWeight() + data.getCost(current.getPoint(), neighbor.getDirection())))
                .filter(neighbor -> !Float.isNaN(neighbor.getWeight()))
                .forEach(queue::add);
    }

    @Override
    public void reset() {
    }

    @Override
    public void doSomeWork() {
    }
}
