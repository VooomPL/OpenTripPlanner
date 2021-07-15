package org.opentripplanner.routing.algorithm.strategies.connection_matrix_heuristic;

import org.opentripplanner.routing.algorithm.strategies.RemainingWeightHeuristic;
import org.opentripplanner.routing.algorithm.strategies.SimpleEuclideanRWH;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.State;

import java.util.Arrays;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;

// TODO VMP-250 finish implementation, add tests
public class ConnectionMatrixRWH implements RemainingWeightHeuristic {

    private static final float INITIAL_WEIGHT = 180.0f; // TODO read from data

    private final RemainingWeightHeuristic fallback = new SimpleEuclideanRWH();

    private ConnectionMatrixHeuristicData data;

    private boolean[][] visited;

    private Queue<PointWithWeight> queue;

    private Point destination;

    @Override
    public void initialize(RoutingRequest options, long abortTime) {
        fallback.initialize(options, abortTime);
        this.data = options.rctx.graph.connectionMatrixHeuristicData;
        visited = new boolean[data.getBoundaries().getHeight()][data.getBoundaries().getWidth()];
        queue = new PriorityQueue<>(data.getBoundaries().getHeight() * data.getBoundaries().getWidth());
        destination = data.mapToPoint(options.to);
    }

    @Override
    public double estimateRemainingWeight(State s) {
        clear();

        Point source = data.mapToPoint(s.getVertex());
        if (!data.getBoundaries().contains(source) || !data.getBoundaries().contains(destination)) {
            return fallback.estimateRemainingWeight(s);
        }
        queue.add(new PointWithWeight(data.mapToPoint(s.getVertex()), INITIAL_WEIGHT));

        while (!queue.isEmpty()) {
            Optional<Float> maybeWeight = iterate();
            if (maybeWeight.isPresent()) {
                return maybeWeight.get();
            }
        }

        return fallback.estimateRemainingWeight(s);
    }

    private void clear() {
        Arrays.stream(visited).forEach(array -> Arrays.fill(array, false));
        queue.clear();
    }

    private Optional<Float> iterate() {
        PointWithWeight current = queue.poll();
        if (current.getPoint().equals(destination)) {
            return Optional.of(current.getWeight());
        }
        if (visited[current.getPoint().getI()][current.getPoint().getJ()]) {
            return Optional.empty();
        }
        visited[current.getPoint().getI()][current.getPoint().getJ()] = true;
        addNeighbors(current);
        return Optional.empty();
    }

    private void addNeighbors(PointWithWeight current) {
        data.getNeighbors(current.getPoint())
                .filter(neighbor -> !visited[neighbor.getPoint().getI()][neighbor.getPoint().getJ()])
                .map(neighbor -> new PointWithWeight(neighbor.getPoint(),
                        current.getWeight() + data.getCost(current.getPoint(), neighbor.getDirection())))
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
