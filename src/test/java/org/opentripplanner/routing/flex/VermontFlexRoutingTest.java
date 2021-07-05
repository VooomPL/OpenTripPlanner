package org.opentripplanner.routing.flex;

import org.junit.Test;
import org.opentripplanner.ConstantsForTests;
import org.opentripplanner.api.model.BoardAlightType;
import org.opentripplanner.routing.algorithm.AStar;
import org.opentripplanner.routing.core.Fare;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.impl.GraphPathFinder;
import org.opentripplanner.routing.services.FareService;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.standalone.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class VermontFlexRoutingTest {

    private static final Logger LOG = LoggerFactory.getLogger(VermontFlexRoutingTest.class);

    private Graph graph = ConstantsForTests.getInstance().getVermontGraph();
    private static final int MAX_WALK_DISTANCE = 804;
    private static final double CALL_AND_RIDE_RELUCTANCE = 3.0;
    private static final double WALK_RELUCTANCE = 3.0;
    private static final double WAIT_AT_BEGINNING_FACTOR = 0;
    private static final int TRANSFER_PENALTY = 600;
    private static final boolean IGNORE_DRT_ADVANCE_MIN_BOOKING = true;

    // Flag stop on both sides, on Jay-Lyn (route 1382)
    @Test
    public void testFlagStop() {
        RoutingRequest options = buildRequest("44.4214596,-72.019371", "44.4277732,-72.01203514",
                "2018-05-23T13:37:00");
        GraphPath path = getPathToDestination(options);
        List<Ride> rides = Ride.createRides(path);
        assertEquals(1, rides.size());
        Ride ride = rides.get(0);
        assertEquals("1382", ride.getRoute().getId());
        assertEquals(BoardAlightType.FLAG_STOP, ride.getBoardType());
        assertEquals(BoardAlightType.FLAG_STOP, ride.getAlightType());
        checkFare(path);
        options.rctx.destroy();
    }

    // Deviated Route on both ends
    @Test
    public void testCallAndRide() {
        RoutingRequest options = buildRequest("44.38485134435363,-72.05881118774415", "44.422379116722084,-72.0198440551758",
                "2018-05-23T13:37:00");
        GraphPath path = getPathToDestination(options);
        List<Ride> rides = Ride.createRides(path);
        assertEquals(1, rides.size());
        Ride ride = rides.get(0);
        assertEquals("7415", ride.getRoute().getId());
        assertEquals(BoardAlightType.DEVIATED, ride.getBoardType());
        assertEquals(BoardAlightType.DEVIATED, ride.getBoardType());
        checkFare(path);

        // Check that times are respected. DAR is available 1pm-3pm

        // arriveBy=false Check start time respected. If request is for before 1pm, we should get a trip starting at 1pm.
        path = getPathToDestination(
                buildRequest("44.38485134435363,-72.05881118774415", "44.422379116722084,-72.0198440551758",
                        "2018-05-23T12:50:00")
        );
        rides = Ride.createRides(path);
        assertEquals(1, rides.size());
        ride = rides.get(0);
        assertEquals("7415", ride.getRoute().getId());
        assertDateEquals(ride.getStartTime(), "2018-05-23T13:00:00");

        // arriveBy=false Check end time respected.
        path = getPathToDestination(
                buildRequest("44.38485134435363,-72.05881118774415", "44.422379116722084,-72.0198440551758",
                        "2018-05-23T14:56:00")
        );
        rides = Ride.createRides(path);
        assertEquals(1, rides.size());
        ride = rides.get(0);
        assertEquals("7415", ride.getRoute().getId());
        assertDateEquals(ride.getStartTime(), "2018-05-24T13:00:00");

        // arriveBy=true Check start time respected.
        path = getPathToDestination(
                buildRequest("44.38485134435363,-72.05881118774415", "44.422379116722084,-72.0198440551758",
                        "2018-05-24T13:05:00", true)
        );
        rides = Ride.createRides(path);
        assertEquals(1, rides.size());
        ride = rides.get(0);
        assertEquals("7415", ride.getRoute().getId());
        assertDateEquals(ride.getEndTime(), "2018-05-23T15:00:00");

        // arriveBy=true Check end time respected
        path = getPathToDestination(
                buildRequest("44.38485134435363,-72.05881118774415", "44.422379116722084,-72.0198440551758",
                        "2018-05-24T15:05:00", true)
        );
        rides = Ride.createRides(path);
        assertEquals(1, rides.size());
        ride = rides.get(0);
        assertEquals("7415", ride.getRoute().getId());
        assertDateEquals(ride.getEndTime(), "2018-05-24T15:00:00");
        options.rctx.destroy();
    }

    // Deviated Fixed Route at both ends
    @Test
    public void testDeviatedFixedRoute() {
        RoutingRequest options = buildRequest("44.950950106914135,-72.20008850097658", "44.94985671536269,-72.13708877563478",
                "2018-05-23T16:00:00");
        GraphPath path = getPathToDestination(options);
        List<Ride> rides = Ride.createRides(path);
        assertEquals(1, rides.size());
        Ride ride = rides.get(0);
        assertEquals("1383", ride.getRoute().getId());
        assertEquals(BoardAlightType.DEVIATED, ride.getBoardType());
        assertEquals(BoardAlightType.DEVIATED, ride.getAlightType());
        checkFare(path);
        options.rctx.destroy();
    }

    // Flag stop to a deviated fixed route that starts as a regular route and ends deviated
    @Test
    public void testFlagStopToRegularStopEndingInDeviatedFixedRoute() {
        RoutingRequest options = buildRequest("44.8091683,-72.20580269999999", "44.94985671536269,-72.13708877563478",
                "2018-06-13T09:30:00");
        GraphPath path = getPathToDestination(options);
        List<Ride> rides = Ride.createRides(path);
        assertEquals(2, rides.size());
        Ride ride = rides.get(0);
        assertEquals("3116", ride.getRoute().getId());
        // TODO AdamWiktor Fix when GTFS-Flex will be needed
        // assertEquals(BoardAlightType.FLAG_STOP, ride.getBoardType());
        assertEquals(BoardAlightType.DEFAULT, ride.getAlightType());

        Ride ride2 = rides.get(1);
        assertEquals("1383", ride2.getRoute().getId());
        assertEquals(BoardAlightType.DEFAULT, ride2.getBoardType());
        assertEquals(BoardAlightType.DEVIATED, ride2.getAlightType());
        checkFare(path);
        options.rctx.destroy();
    }

    // Deviated Route on both ends
    @Test
    public void testMultipleThreads() throws InterruptedException, ExecutionException {
        AtomicBoolean thread1SetupComplete = new AtomicBoolean(false),
                thread2SearchComplete = new AtomicBoolean(false);

        FutureTask<Boolean> task1 = new FutureTask<>(() -> {
            boolean success = true;
            LOG.info("Thread 1 starting");
            RoutingRequest options = buildRequest("44.4214596,-72.019371", "44.4277732,-72.01203514",
                    "2018-05-23T13:37:00");
            GraphPath path = getPathToDestination(options);
            thread1SetupComplete.set(true);
            List<Ride> rides = Ride.createRides(path);
            assertEquals(1, rides.size());
            Ride ride = rides.get(0);
            success &= "1382".equals(ride.getRoute().getId());
            success &= BoardAlightType.FLAG_STOP.equals(ride.getBoardType());
            success &= BoardAlightType.FLAG_STOP.equals(ride.getAlightType());
            while (!thread2SearchComplete.get()) ;
            LOG.info("Thread 1 cleaning up...");
            options.rctx.destroy();
            LOG.info("Thread 1 complete.");
            return success;
        });

        FutureTask<Boolean> task2 = new FutureTask<>(() -> {
            boolean success = true;
            while (!thread1SetupComplete.get()) ;
            LOG.info("Thread 2 starting");
            RoutingRequest options = buildRequest("44.4214596,-72.019371", "44.4277732,-72.01203514",
                    "2018-05-23T13:37:00");
            // Don't make temporary edges
            AStar astar = new AStar();
            astar.getShortestPathTree(options);
            GraphPath path = astar.getPathsToTarget().size() > 0 ? astar.getPathsToTarget().get(0) : null;
            List<Ride> rides = Ride.createRides(path);
            // possibly no rides if just walking.
            if (!rides.isEmpty()) {
                Ride ride = rides.get(0);
                success &= !BoardAlightType.FLAG_STOP.equals(ride.getBoardType());
                success &= !BoardAlightType.FLAG_STOP.equals(ride.getAlightType());
            }
            LOG.info("Thread 2 cleaning up...");
            options.rctx.destroy();
            LOG.info("Thread 2 complete");
            thread2SearchComplete.set(true);
            return success;
        });

        new Thread(task1).start();
        new Thread(task2).start();

        assertTrue(task1.get());
        assertTrue(task2.get());
    }

    private RoutingRequest buildRequest(String from, String to, String datetime) {
        return buildRequest(from, to, datetime, false);
    }

    private RoutingRequest buildRequest(String from, String to, String datetime, boolean arriveBy) {
        return buildRequest(from, to, datetime, arriveBy, MAX_WALK_DISTANCE,
                CALL_AND_RIDE_RELUCTANCE, WALK_RELUCTANCE, WAIT_AT_BEGINNING_FACTOR,
                TRANSFER_PENALTY, IGNORE_DRT_ADVANCE_MIN_BOOKING);
    }

    private RoutingRequest buildRequest(String from, String to, String datetime, boolean arriveBy,
                                        int maxWalkDistance, double callAndRideReluctance, double walkReluctance,
                                        double waitAtBeginningFactor, int transferPenalty,
                                        boolean ignoreDrtAdvanceMinBooking) {
        RoutingRequest options = new RoutingRequest();
        options.setArriveBy(arriveBy);
        // defaults in vermont router-config.json
        options.setMaxWalkDistance(maxWalkDistance);
        options.setSoftWalkLimit(true);
        options.flex.setCallAndRideReluctance(callAndRideReluctance);
        options.routingReluctances.setWalkReluctance(walkReluctance);
        options.routingReluctances.setWaitAtBeginningFactor(waitAtBeginningFactor);
        options.routingPenalties.setTransferPenalty(transferPenalty);

        // for testing
        options.flex.setIgnoreDrtAdvanceBookMin(ignoreDrtAdvanceMinBooking);

        options.setDateTime(LocalDateTime.parse(datetime).atZone(graph.getTimeZone().toZoneId()).toInstant());
        options.setFromString(from);
        options.setToString(to);
        options.setRoutingContext(graph);

        return options;
    }


    private GraphPath getPathToDestination(RoutingRequest options) {
        Router router = new Router("default", graph);
        return new GraphPathFinder(router).getPaths(options).get(0);
    }

    private void checkFare(GraphPath path) {
        // test fare calculated correctly
        FareService fareService = graph.getService(FareService.class);
        Fare cost = fareService.getCost(path);
        assertNotNull(cost);
        assertEquals("920", cost.getDetails(Fare.FareType.regular).iterator().next().fareId.getId());
    }

    private void assertDateEquals(long timeSeconds, String datetime) {
        assertEquals(LocalDateTime.parse(datetime).atZone(graph.getTimeZone().toZoneId()).toInstant(), Instant.ofEpochSecond(timeSeconds));
    }
}
