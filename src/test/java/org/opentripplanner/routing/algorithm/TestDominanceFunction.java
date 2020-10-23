package org.opentripplanner.routing.algorithm;

import junit.framework.TestCase;
import org.junit.Assert;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateEditor;
import org.opentripplanner.routing.core.routing_parametrizations.RoutingStateDiffOptions;
import org.opentripplanner.routing.core.vehicle_sharing.KickScooterDescription;
import org.opentripplanner.routing.core.vehicle_sharing.Provider;
import org.opentripplanner.routing.edgetype.SimpleTransfer;
import org.opentripplanner.routing.edgetype.TimedTransferEdge;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.spt.DominanceFunction;
import org.opentripplanner.routing.vertextype.TransitStopArrive;
import org.opentripplanner.routing.vertextype.TransitStopDepart;

import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.Mockito.mock;


public class TestDominanceFunction extends TestCase {

    public void testGeneralDominanceFunction() {
        DominanceFunction minimumWeightDominanceFunction = new DominanceFunction.MinimumWeight();
        Vertex fromVertex = mock(TransitStopArrive.class);
        Vertex toVertex = mock(TransitStopDepart.class);
        RoutingRequest request = new RoutingRequest();

        // Test if domination works in the general case

        State stateA = new State(fromVertex, null, 0, request);
        State stateB = new State(toVertex, null, 0, request);
        stateA.weight = 1;
        stateB.weight = 2;

        Assert.assertTrue(minimumWeightDominanceFunction.betterOrEqualAndComparable(stateA, stateB));
        Assert.assertFalse(minimumWeightDominanceFunction.betterOrEqualAndComparable(stateB, stateA));

        // Simple transfers should not dominate

        SimpleTransfer simpleTransfer = mock(SimpleTransfer.class);
        State stateC = new State(fromVertex, simpleTransfer, 0, request);
        State stateD = new State(toVertex, null, 0, request);
        stateC.weight = 1;
        stateD.weight = 2;

        Assert.assertFalse(minimumWeightDominanceFunction.betterOrEqualAndComparable(stateC, stateD));
        Assert.assertFalse(minimumWeightDominanceFunction.betterOrEqualAndComparable(stateD, stateC));

        // Timed transfers should not dominate

        TimedTransferEdge timedTransferEdge = mock(TimedTransferEdge.class);
        State stateE = new State(fromVertex, timedTransferEdge, 0, request);
        State stateF = new State(toVertex, null, 0, request);
        stateE.weight = 1;
        stateF.weight = 2;

        Assert.assertFalse(minimumWeightDominanceFunction.betterOrEqualAndComparable(stateE, stateF));
        Assert.assertFalse(minimumWeightDominanceFunction.betterOrEqualAndComparable(stateF, stateE));
    }

    private void correctDifferByRange(Double rangeA, Double rangeB, int timeA, int timeB, int weightA, int weightB, boolean stateABetterOrEqualToB) {
        DominanceFunction minimumWeightDominanceFunction = new DominanceFunction.MinimumWeight();
        RoutingStateDiffOptions routingStateDiffOptions = new RoutingStateDiffOptions();
        routingStateDiffOptions.setKickscooterRangeGroupsInMeters((new ArrayList<Double>(Arrays.asList(5000D, 1000D))));

        Vertex fromVertex = mock(TransitStopArrive.class);
        Vertex toVertex = mock(TransitStopDepart.class);
        RoutingRequest request = new RoutingRequest();
        request.routingStateDiffOptions = routingStateDiffOptions;


        State stateA = new State(fromVertex, null, timeA, request);
        State stateB = new State(toVertex, null, timeB, request);

        stateA.weight = weightA;
        stateB.weight = weightB;

        Provider provider = new Provider();
        KickScooterDescription kickScooterA = new KickScooterDescription("id1", 0, 0, null, null, provider, rangeA);
        KickScooterDescription kickScooterB = new KickScooterDescription("id2", 0, 0, null, null, provider, rangeB);

        StateEditor editorA = stateA.edit(null);
        StateEditor editorB = stateB.edit(null);
        editorA.beginVehicleRenting(kickScooterA);
        editorB.beginVehicleRenting(kickScooterB);
        stateA = editorA.makeState();
        stateB = editorB.makeState();

        assertEquals(minimumWeightDominanceFunction.betterOrEqualAndComparable(stateA, stateB), stateABetterOrEqualToB);

    }

    public void testDifferingRangeGroups() {
//      A has worse range but better time and weight
        correctDifferByRange(500D, 1500D, 0, 2000, 0, 2000, false);
        correctDifferByRange(1500D, 5500D, 0, 2000, 0, 2000, false);
//      Same but swapped
        correctDifferByRange(1500D, 500D, 2000, 0, 2000, 0, false);
        correctDifferByRange(5500D, 1500D, 2000, 0, 2000, 0, false);
//      State A has worse range and weight
        correctDifferByRange(500D, 1500D, 0, 0, 10, 0, false);
        correctDifferByRange(4500D, 5500D, 0, 0, 10, 0, false);
//      State A has better range and weight
        correctDifferByRange(1500D, 500D, 0, 10, 0, 10, true);
        correctDifferByRange(5500D, 4500D, 0, 10, 0, 10, true);
    }

    // TODO: Make unit tests for rest of dominance functionality
    // TODO: Make functional tests for concepts covered by dominance with current algorithm
    // (Specific transfers, bike rental, park and ride, turn restrictions)
}
