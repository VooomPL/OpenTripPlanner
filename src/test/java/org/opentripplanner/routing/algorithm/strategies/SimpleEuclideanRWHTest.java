package org.opentripplanner.routing.algorithm.strategies;

import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.routing.core.vehicle_sharing.CarDescription;
import org.opentripplanner.routing.core.vehicle_sharing.KickScooterDescription;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleType;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleTypeFilter;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.location.TemporaryStreetLocation;

import java.util.Set;

import static org.junit.Assert.assertEquals;

public class SimpleEuclideanRWHTest {

    private Graph graph;
    private Vertex source, destination;
    private SimpleEuclideanRWH heuristic;

    @Before
    public void setUp() {
        graph = new Graph();
        source = new TemporaryStreetLocation("source", new Coordinate(0, 0), null, false);
        destination = new TemporaryStreetLocation("destination", new Coordinate(0, 1), null, true);
        heuristic = new SimpleEuclideanRWH();
    }

    @Test
    public void shouldReturnCorrectMultiplierForWalkOnlyRequest() {
        // given
        RoutingRequest options = new RoutingRequest();
        options.setRoutingContext(graph, source, destination);
        options.setModes(new TraverseModeSet(TraverseMode.WALK));

        // when
        heuristic.initialize(options, 1);

        // then
        assertEquals(options.routingReluctances.getWalkReluctance() / options.walkSpeed, heuristic.getBestMultiplier(), 0.0001);
    }

    @Test
    public void shouldReturnCorrectMultiplierForWalkAndTransitRequest() {
        // given
        RoutingRequest options = new RoutingRequest();
        options.setRoutingContext(graph, source, destination);
        options.setModes(new TraverseModeSet(TraverseMode.WALK, TraverseMode.TRANSIT));

        // when
        heuristic.initialize(options, 1);

        // then
        assertEquals(1.0 / options.carSpeed, heuristic.getBestMultiplier(), 0.0001);
    }

    @Test
    public void shouldReturnCorrectMultiplierForOwnCarRequest() {
        // given
        RoutingRequest options = new RoutingRequest();
        options.setRoutingContext(graph, source, destination);
        options.setModes(new TraverseModeSet(TraverseMode.WALK, TraverseMode.CAR));

        // when
        heuristic.initialize(options, 1);

        // then
        assertEquals(options.routingReluctances.getCarReluctance() / options.carSpeed, heuristic.getBestMultiplier(), 0.0001);
    }

    @Test
    public void shouldReturnCorrectMultiplierForOwnBicycleRequest() {
        // given
        RoutingRequest options = new RoutingRequest();
        options.setRoutingContext(graph, source, destination);
        options.setModes(new TraverseModeSet(TraverseMode.WALK, TraverseMode.BICYCLE));

        // when
        heuristic.initialize(options, 1);

        // then
        assertEquals(options.routingReluctances.getBicycleReluctance() / options.bikeSpeed, heuristic.getBestMultiplier(), 0.0001);
    }

    @Test
    public void shouldReturnCorrectMultiplierForRentingAllowedRequest() {
        // given
        RoutingRequest options = new RoutingRequest();
        options.setRoutingContext(graph, source, destination);
        options.setModes(new TraverseModeSet(TraverseMode.WALK));
        options.rentingAllowed = true;

        // when
        heuristic.initialize(options, 1);

        // then
        assertEquals(options.routingReluctances.getCarReluctance() / CarDescription.getMaxPossibleSpeed(), heuristic.getBestMultiplier(), 0.0001);
    }

    @Test
    public void shouldReturnCorrectMultiplierForRentingAllowedAndTransitRequest() {
        // given
        RoutingRequest options = new RoutingRequest();
        options.setRoutingContext(graph, source, destination);
        options.setModes(new TraverseModeSet(TraverseMode.WALK, TraverseMode.TRANSIT));
        options.rentingAllowed = true;

        // when
        heuristic.initialize(options, 1);

        // then
        assertEquals(options.routingReluctances.getCarReluctance() / CarDescription.getMaxPossibleSpeed(), heuristic.getBestMultiplier(), 0.0001);
    }

    @Test
    public void shouldReturnCorrectMultiplierForRentingAllowedKickscooterOnlyRequest() {
        // given
        RoutingRequest options = new RoutingRequest();
        options.setRoutingContext(graph, source, destination);
        options.setModes(new TraverseModeSet(TraverseMode.WALK));
        options.rentingAllowed = true;
        options.vehicleValidator.addFilter(new VehicleTypeFilter(Set.of(VehicleType.KICKSCOOTER)));

        // when
        heuristic.initialize(options, 1);

        // then
        assertEquals(options.routingReluctances.getKickScooterReluctance() / KickScooterDescription.getMaxPossibleSpeed(), heuristic.getBestMultiplier(), 0.0001);
    }
}
