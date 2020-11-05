package org.opentripplanner.routing.spt;

import org.opentripplanner.routing.core.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class ForbiddenStatesSelector {
    private static final Logger LOG = LoggerFactory.getLogger(ForbiddenStatesSelector.class);

    StateFeature forbiddenFeature;
    LinkedList<State> consumedStates;
    LinkedList<State> allowedStates;


    public ForbiddenStatesSelector(StateFeature forbiddenFeature, LinkedList<State> allGeneratedStates, LinkedList<State> consumedStates) {
        this.forbiddenFeature = forbiddenFeature;
        this.consumedStates = consumedStates;
        long ts1 = System.currentTimeMillis();

        State startingState = consumedStates.stream().filter(s -> s.weight == 0).findAny().get();
        long ts2 = System.currentTimeMillis();

        HashMap<State, Boolean> stateHasFeature = new HashMap<>();
        stateHasFeature.put(startingState, false);

        allGeneratedStates.forEach(s -> backtrackAndSearchForFeature(s, stateHasFeature));
        long ts3 = System.currentTimeMillis();

        allowedStates = new LinkedList<>(stateHasFeature.keySet()).stream().filter(s -> !stateHasFeature.get(s)).collect(Collectors.toCollection(LinkedList::new));
        long ts4 = System.currentTimeMillis();


        allowedStates.removeIf(State::isConsumed);

//        removeAll(allowedStates, consumedStates);
        long ts5 = System.currentTimeMillis();
        LOG.info("Times : {} {} {} {} ", ts2 - ts1, ts3 - ts2, ts4 - ts3, ts5 - ts4);

//        allowedStates.removeAll(consumedStates);
    }

    private void removeAll(LinkedList<State> listA, LinkedList<State> listB) {
        LinkedList<State> newListA = new LinkedList<>();
        LinkedList<State> newListB = new LinkedList<>();
        long ts1 = System.currentTimeMillis();

        TreeSet<State> valuesInB = new TreeSet<>((s1, s2) -> {
            if (s1 == s2) return 0;
            if (s1.getWeight() < s2.getWeight()) return -1;
            else return 1;
        });
        valuesInB.addAll(listB);


        listA.removeIf(valuesInB::contains);
//        listA.sort(Comparator.comparing(State::getWeight));
//        listB.sort(Comparator.comparing(State::getWeight));
        long ts2 = System.currentTimeMillis();
//
//        while (!listA.isEmpty() && !listB.isEmpty()) {
//            State a = listA.get(0);
//            State b = listB.get(0);
//            if (a == b) {
//                newListB.add(b);
//                listA.remove(0);
//                listB.remove(0);
//            } else if (a.weight < b.weight) {
//                newListA.add(a);
//                listA.remove(0);
//            } else if (a.weight > b.weight) {
//                newListB.add(b);
//                listB.remove(0);
//            } else if (a.weight == b.weight) {
//                newListA.add(a);
//                newListB.add(b);
//                listA.remove(0);
//                listB.remove(0);
//            }
//        }
//        long ts3 = System.currentTimeMillis();
//
//        newListA.addAll(listA);
//        newListB.addAll(listB);
//        listA.clear();
//        listB.clear();
//        listA.addAll(newListA);
//        listB.addAll(newListB);
//        long ts4 = System.currentTimeMillis();
        LOG.info("Removing times  : {}", ts2 - ts1);


    }

    private boolean backtrackAndSearchForFeature(State state, HashMap<State, Boolean> stateHasFeature) {
        if (stateHasFeature.containsKey(state)) {
            return stateHasFeature.get(state);
        }
        if (forbiddenFeature.doesStateHaveFeature(state)) {
            stateHasFeature.put(state, true);
            return true;
        }
        boolean result = backtrackAndSearchForFeature(state.getBackState(), stateHasFeature);
        stateHasFeature.put(state, result);

        return result;
    }

    // Should return all generated and unconsumed states without a forbidden feature.
    public List<State> allowedStates() {
        return allowedStates;
    }

}
