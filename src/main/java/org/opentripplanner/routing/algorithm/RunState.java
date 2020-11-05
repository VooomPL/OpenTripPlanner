package org.opentripplanner.routing.algorithm;

import org.opentripplanner.common.pqueue.BinHeap;
import org.opentripplanner.routing.algorithm.strategies.RemainingWeightHeuristic;
import org.opentripplanner.routing.algorithm.strategies.SearchTerminationStrategy;
import org.opentripplanner.routing.core.RoutingContext;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleFilter;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.spt.ForbiddenStatesSelector;
import org.opentripplanner.routing.spt.ShortestPathTree;
import org.opentripplanner.routing.spt.StateFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/* TODO instead of having a separate class for search state, we should just make one GenericAStar per request. */
class RunState {
    private static final Logger LOG = LoggerFactory.getLogger(RunState.class);

    public State u;
    public ShortestPathTree spt;
    BinHeap<State> pq;
    RemainingWeightHeuristic heuristic;
    public RoutingContext rctx;
    public int nVisited;
    public List<State> targetAcceptedStates;
    //    public AStar.RunStatus status;
    public RoutingRequest options;
    public SearchTerminationStrategy terminationStrategy;
    public Vertex u_vertex;
    Double foundPathWeight = null;
    public LinkedList<State> consumedStates;
    public LinkedList<State> allGeneratedStates;
    public List<VehicleFilter> addedFilters;

    public RunState(RoutingRequest options, SearchTerminationStrategy terminationStrategy) {
        this.options = options;
        this.terminationStrategy = terminationStrategy;
        spt = options.getNewShortestPathTree();
        consumedStates = new LinkedList<>();
        allGeneratedStates = new LinkedList<>();
        addedFilters = new LinkedList<>();

    }

    //  TODO allow multiple resets of RunState. Right now second reset may cause random side effects.
    public void resetState(StateFeature forbiddenFeature) {
        LOG.info("Reseting RunState {} generated states, {} consumed states, unconsumed {} states", allGeneratedStates.size(), consumedStates.size(), allGeneratedStates.size() - consumedStates.size());
        u = consumedStates.stream().filter(s -> s.weight == 0).findAny().get();
        long start = System.currentTimeMillis();
        ForbiddenStatesSelector forbiddenStatesSelector = new ForbiddenStatesSelector(forbiddenFeature, allGeneratedStates, consumedStates);
        long start2 = System.currentTimeMillis();

        spt = new ShortestPathTree(options, spt.dominanceFunction);
        pq = new BinHeap<>();
        nVisited = 0;
        forbiddenStatesSelector.allowedStates().forEach(s -> {
            if (spt.add(s)) {
                double remaining_w = heuristic.estimateRemainingWeight(s);
                double estimate = s.getWeight() + remaining_w * options.remainingWeightWeight;
                pq.insert(s, estimate);
            }
        });
        long finish = System.currentTimeMillis();

        LOG.info("Reused {} unconsumed states in {} {} miliseconds", pq.size(), start2 - start, finish - start2);

    }

}
