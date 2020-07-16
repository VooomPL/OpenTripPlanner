package org.opentripplanner.graph_builder.linking;

import gnu.trove.map.TIntDoubleMap;
import jersey.repackaged.com.google.common.collect.Lists;
import org.opentripplanner.common.geometry.SphericalDistanceLibrary;

import java.util.List;
import java.util.function.Function;

// TODO: Adam Wiktor make it non static!!!!!!
public class EdgesFinderUtils {

    /**
     * if there are two ways and the distances to them differ by less than this value, we link to both of them
     */
    private static final double DUPLICATE_WAY_EPSILON_DEGREES = SphericalDistanceLibrary.metersToDegrees(0.001);

    /**
     * sort a list of edges/vertices based on precomputed distances
     */
    public static <T> void sort(List<T> candidates, TIntDoubleMap distances, Function<T, Integer> indexGetter) {
        candidates.sort((o1, o2) -> {
            double diff = distances.get(indexGetter.apply(o1)) - distances.get(indexGetter.apply(o2));
            if (diff < 0) {
                return -1;
            }
            if (diff > 0) {
                return 1;
            }
            return 0;
        });
    }

    /**
     * get edges/vertices with lowest distance
     */
    public static <T> List<T> getBestCandidates(List<T> candidates, TIntDoubleMap distances,
                                                Function<T, Integer> indexGetter) {
        List<T> bestCandidates = Lists.newArrayList();
        // Add elements until there is a break of epsilon meters.
        // we do this to enforce determinism. if there are a lot of stops that are all extremely close to each other,
        // we want to be sure that we deterministically link to the same ones every time. Any hard cutoff means things can
        // fall just inside or beyond the cutoff depending on floating-point operations.
        int i = 0;
        do {
            bestCandidates.add(candidates.get(i++));
        } while (i < candidates.size() && shouldAddNextCandidate(candidates, distances, indexGetter, i));
        return bestCandidates;
    }

    private static <T> boolean shouldAddNextCandidate(List<T> candidates, TIntDoubleMap distances,
                                                      Function<T, Integer> indexGetter, int nextId) {
        int currentIndex = indexGetter.apply(candidates.get(nextId - 1));
        int nextIndex = indexGetter.apply(candidates.get(nextId));
        return distances.get(nextIndex) - distances.get(currentIndex) < DUPLICATE_WAY_EPSILON_DEGREES;
    }
}
