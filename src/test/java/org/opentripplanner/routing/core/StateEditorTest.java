package org.opentripplanner.routing.core;

import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.opentripplanner.routing.algorithm.costs.CostFunction;
import org.opentripplanner.routing.algorithm.profile.OptimizationProfile;
import org.opentripplanner.routing.algorithm.strategies.RemainingWeightHeuristic;
import org.opentripplanner.routing.core.vehicle_sharing.CarDescription;
import org.opentripplanner.routing.core.vehicle_sharing.FuelType;
import org.opentripplanner.routing.core.vehicle_sharing.Gearbox;
import org.opentripplanner.routing.core.vehicle_sharing.Provider;
import org.opentripplanner.routing.core.vehicle_sharing.VehiclePricingPackage;
import org.opentripplanner.routing.edgetype.rentedgetype.DropoffVehicleEdge;
import org.opentripplanner.routing.edgetype.rentedgetype.RentVehicleEdge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.impl.StreetVertexIndexServiceImpl;
import org.opentripplanner.routing.spt.DominanceFunction;
import org.opentripplanner.routing.vertextype.TemporaryRentVehicleVertex;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class StateEditorTest {

    private static final CarDescription CAR_1 = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(2, "PANEK"));
    private static final double DELTA = 0.1;

    private State state, rentingState;
    private RoutingRequest request;
    private RoutingRequest priceOptimizeRequest;
    private List<VehiclePricingPackage> availablePricingPackages;
    private TemporaryRentVehicleVertex rentVehicleVertex;
    private RentVehicleEdge rentVehicleEdge;
    private DropoffVehicleEdge dropoffVehicleEdge;

    @Before
    public void setUp() {
        Graph graph = new Graph();
        configurePricingPackages();
        rentVehicleVertex = new TemporaryRentVehicleVertex("id", new Coordinate(1, 2), "name");
        rentVehicleEdge = new RentVehicleEdge(rentVehicleVertex, CAR_1);
        dropoffVehicleEdge = new DropoffVehicleEdge(rentVehicleVertex);
        request = new RoutingRequest();
        request.setDummyRoutingContext(graph);
        request.setModes(new TraverseModeSet(TraverseMode.WALK, TraverseMode.CAR));
        request.setStartingMode(TraverseMode.WALK);
        priceOptimizeRequest = new RoutingRequest();
        configurePriceBasedOptimizationProfile();
        state = new State(rentVehicleVertex, request);
        StateEditor se = state.edit(rentVehicleEdge);
        se.beginVehicleRenting(CAR_1);
        rentingState = se.makeState();
    }

    private void configurePriceBasedOptimizationProfile() {
        priceOptimizeRequest.setOptimizationProfile(new OptimizationProfile() {
            @Override
            public CostFunction getCostFunction() {
                return category -> category.equals(CostFunction.CostCategory.PRICE_ASSOCIATED) ? 1 : 0;
            }

            @Override
            public DominanceFunction getDominanceFunction() {
                return null;
            }

            @Override
            public RemainingWeightHeuristic getHeuristic() {
                return null;
            }

            @Override
            public RemainingWeightHeuristic getReversedSearchHeuristic() {
                return null;
            }
        });
    }

    private void configurePricingPackages() {
        VehiclePricingPackage defaultPackage = new VehiclePricingPackage();
        availablePricingPackages = new ArrayList<>();

        VehiclePricingPackage pricingPackage = new VehiclePricingPackage(BigDecimal.valueOf(9.99), 480,
                defaultPackage.getFreeSeconds(), BigDecimal.valueOf(13), defaultPackage.getStartPrice(),
                defaultPackage.getDrivingPricePerTimeTickInPackage(), defaultPackage.getParkingPricePerTimeTickInPackage(),
                BigDecimal.valueOf(1.29), defaultPackage.getParkingPricePerTimeTickInPackageExceeded(), BigDecimal.valueOf(0.3),
                defaultPackage.getSecondsPerTimeTickInPackage(), defaultPackage.getSecondsPerTimeTickInPackageExceeded(),
                BigDecimal.valueOf(199), defaultPackage.isKilometerPriceEnabledAboveMaxRentingPrice());
        availablePricingPackages.add(pricingPackage);

        pricingPackage = new VehiclePricingPackage(BigDecimal.valueOf(9.89), 480,
                defaultPackage.getFreeSeconds(), BigDecimal.valueOf(15), defaultPackage.getStartPrice(),
                defaultPackage.getDrivingPricePerTimeTickInPackage(), defaultPackage.getParkingPricePerTimeTickInPackage(),
                BigDecimal.valueOf(2.0), defaultPackage.getParkingPricePerTimeTickInPackageExceeded(), BigDecimal.valueOf(0.8),
                defaultPackage.getSecondsPerTimeTickInPackage(), defaultPackage.getSecondsPerTimeTickInPackageExceeded(),
                BigDecimal.valueOf(199), defaultPackage.isKilometerPriceEnabledAboveMaxRentingPrice());
        availablePricingPackages.add(pricingPackage);
    }

    @Test
    public void testIncrementTimeInSeconds() {
        RoutingRequest routingRequest = new RoutingRequest();
        StateEditor stateEditor = new StateEditor(routingRequest, null);

        stateEditor.setTimeSeconds(0);
        stateEditor.incrementTimeInSeconds(999999999);

        assertEquals(999999999, stateEditor.child.getTimeSeconds());
    }

    /**
     * Test update of non transit options.
     */
    @Test
    public void testSetNonTransitOptionsFromState() {
        RoutingRequest request = new RoutingRequest();
        request.setMode(TraverseMode.CAR);
        request.parkAndRide = true;
        Graph graph = new Graph();
        graph.streetIndex = new StreetVertexIndexServiceImpl(graph);
        request.rctx = new RoutingContext(request, graph);
        State state = new State(request);

        state.stateData.carParked = true;
        state.stateData.bikeParked = true;
        state.stateData.usingRentedBike = false;
        state.stateData.currentTraverseMode = TraverseMode.WALK;

        StateEditor se = new StateEditor(request, null);
        se.setNonTransitOptionsFromState(state);
        State updatedState = se.makeState();
        assertEquals(TraverseMode.WALK, updatedState.getNonTransitMode());
        assertTrue(updatedState.isCarParked());
        assertTrue(updatedState.isBikeParked());
        assertFalse(updatedState.isBikeRenting());
    }

    @Test
    public void shouldAddLowestPackagePriceToWeight() {
        // given
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(13, "Innogy"), 1000.0, availablePricingPackages.get(0));
        car.getVehiclePricingPackages().add(availablePricingPackages.get(1));

        StateEditor stateEditor = new StateEditor(priceOptimizeRequest, rentVehicleVertex);

        // when
        stateEditor.beginVehicleRenting(car);

        // then
        assertEquals(availablePricingPackages.get(1).getPackagePrice().doubleValue(), stateEditor.child.weight, 0);
        assertEquals(1, stateEditor.child.getActivePackageIndex());
    }

    @Test
    public void shouldIncreaseWeightToMatchLowestMinimumRentingPrice() {
        // given
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(13, "Innogy"), 1000.0, availablePricingPackages.get(0));
        car.getVehiclePricingPackages().add(availablePricingPackages.get(1));

        StateEditor stateEditor = new StateEditor(priceOptimizeRequest, rentVehicleVertex);

        // when
        stateEditor.beginVehicleRenting(car);
        stateEditor.incrementTimeInSeconds(60);
        stateEditor.doneVehicleRenting();

        // then
        int expectedNewPricingPackageIndex = 0;
        VehiclePricingPackage expectedNewPricingPackage = availablePricingPackages.get(expectedNewPricingPackageIndex);
        assertEquals(expectedNewPricingPackage.getMinRentingPrice().doubleValue(), stateEditor.child.weight, 0);
        assertEquals(expectedNewPricingPackageIndex, stateEditor.child.getActivePackageIndex());
    }

    @Test
    public void shouldAddLowestTotalPriceToWeightWhenDistanceIncreased() {
        // given
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(13, "Innogy"), 1000.0, availablePricingPackages.get(0));
        car.getVehiclePricingPackages().add(availablePricingPackages.get(1));
        StateEditor stateEditor = new StateEditor(priceOptimizeRequest, null);

        // when
        stateEditor.beginVehicleRenting(car);
        stateEditor.incrementWalkDistanceInMeters(1065.174);

        // then
        int expectedNewPricingPackageIndex = 0;
        VehiclePricingPackage expectedNewPricingPackage = availablePricingPackages.get(expectedNewPricingPackageIndex);
        BigDecimal expectedNewPrice = expectedNewPricingPackage.getPackagePrice().add(expectedNewPricingPackage.getKilometerPrice());
        assertEquals(expectedNewPrice.doubleValue(), stateEditor.child.weight, 0.001);
        assertEquals(expectedNewPricingPackageIndex, stateEditor.child.getActivePackageIndex());
    }

    @Test
    public void shouldAddLowestTotalPriceToWeightWhenTimeIncreased() {
        // given
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(13, "Innogy"), 1000.0, availablePricingPackages.get(0));
        car.getVehiclePricingPackages().add(availablePricingPackages.get(1));
        StateEditor stateEditor = new StateEditor(priceOptimizeRequest, null);

        // when
        stateEditor.beginVehicleRenting(car);
        stateEditor.incrementTimeInSeconds(485);

        // then
        int expectedNewPricingPackageIndex = 0;
        VehiclePricingPackage expectedNewPricingPackage = availablePricingPackages.get(expectedNewPricingPackageIndex);
        BigDecimal expectedNewPrice = expectedNewPricingPackage.getPackagePrice().add(expectedNewPricingPackage.getDrivingPricePerTimeTickInPackageExceeded());
        assertEquals(expectedNewPrice.doubleValue(), stateEditor.child.weight, 0.001);
        assertEquals(expectedNewPricingPackageIndex, stateEditor.child.getActivePackageIndex());
    }

    @Test
    public void shouldNotModifyWeightWhenTimeIncreasedWithinPackage() {
        // given
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(13, "Innogy"), 1000.0, availablePricingPackages.get(0));
        car.getVehiclePricingPackages().add(availablePricingPackages.get(1));
        StateEditor stateEditor = new StateEditor(priceOptimizeRequest, null);

        // when
        stateEditor.beginVehicleRenting(car);
        stateEditor.incrementTimeInSeconds(60);

        // then
        int expectedNewPricingPackageIndex = 1;
        VehiclePricingPackage expectedNewPricingPackage = availablePricingPackages.get(expectedNewPricingPackageIndex);
        assertEquals(expectedNewPricingPackage.getPackagePrice().doubleValue(), stateEditor.child.weight, 0.001);
        assertEquals(expectedNewPricingPackageIndex, stateEditor.child.getActivePackageIndex());
    }

    @Test
    public void shouldNotCreateStateWithNewPackageConfigDueToNegativeWeightIncrement() {
        // given
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(13, "Innogy"), 1000.0, availablePricingPackages.get(0));
        car.getVehiclePricingPackages().add(availablePricingPackages.get(1));
        StateEditor stateEditor = new StateEditor(priceOptimizeRequest, null);

        // when
        stateEditor.beginVehicleRenting(car);
        stateEditor.incrementTimeInSeconds(485);
        // artificially generating negative weight increment error...
        VehiclePricingPackage oldPricingPackage = car.getVehiclePricingPackage(1);
        car.getVehiclePricingPackages().set(1, new VehiclePricingPackage(oldPricingPackage.getPackagePrice(),
                oldPricingPackage.getPackageTimeLimitInSeconds(), oldPricingPackage.getFreeSeconds(),
                oldPricingPackage.getMinRentingPrice(), oldPricingPackage.getStartPrice(),
                oldPricingPackage.getDrivingPricePerTimeTickInPackage(), oldPricingPackage.getParkingPricePerTimeTickInPackage(),
                BigDecimal.valueOf(0.1), oldPricingPackage.getParkingPricePerTimeTickInPackageExceeded(),
                oldPricingPackage.getKilometerPrice(), oldPricingPackage.getSecondsPerTimeTickInPackage(),
                oldPricingPackage.getSecondsPerTimeTickInPackageExceeded(), oldPricingPackage.getMaxRentingPrice(),
                oldPricingPackage.isKilometerPriceEnabledAboveMaxRentingPrice()));

        stateEditor.incrementTimeInSeconds(60);

        // then
        assertNull(stateEditor.makeState());
    }

    @Test
    public void shouldAllowRentingVehicles() {
        // given
        StateEditor stateEditor = state.edit(rentVehicleEdge);

        // when
        stateEditor.beginVehicleRenting(CAR_1);
        State next = stateEditor.makeState();

        // then
        assertEquals(TraverseMode.CAR, next.getNonTransitMode());
        assertEquals(CAR_1, next.getCurrentVehicle());
        assertEquals(0, next.distanceTraversedInCurrentVehicle, DELTA);
        assertEquals(state.time + request.routingDelays.getRentingTime(CAR_1) * 1000, next.time);
        assertEquals(state.weight +
                request.routingDelays.getRentingTime(CAR_1) * request.routingReluctances.getRentingReluctance() +
                request.routingPenalties.getRentingVehiclePenalty(), next.weight, DELTA);
    }

    @Test
    public void shouldAllowDroppingOffVehicles() {
        // given
        StateEditor stateEditor = rentingState.edit(dropoffVehicleEdge);

        // when
        stateEditor.doneVehicleRenting();
        State next = stateEditor.makeState();

        // then
        assertEquals(TraverseMode.WALK, next.getNonTransitMode());
        assertNull(next.getCurrentVehicle());
        assertEquals(rentingState.time + request.routingDelays.getDropoffTime(CAR_1) * 1000, next.time);
        assertEquals(rentingState.weight + request.routingDelays.getDropoffTime(CAR_1) * request.routingReluctances.getRentingReluctance(), next.weight, DELTA);
    }

    @Test
    public void shouldAllowReverseRentingVehicles() {
        // given
        StateEditor stateEditor = rentingState.edit(rentVehicleEdge);

        // when
        stateEditor.reversedBeginVehicleRenting();
        State next = stateEditor.makeState();

        // then: we drop off a car, but in renting time
        assertEquals(TraverseMode.WALK, next.getNonTransitMode());
        assertNull(next.getCurrentVehicle());
        assertEquals(rentingState.time + request.routingDelays.getRentingTime(CAR_1) * 1000, next.time);
    }

    @Test
    public void shouldAllowReverseDroppingOffVehicles() {
        // given
        StateEditor stateEditor = state.edit(dropoffVehicleEdge);

        // when
        stateEditor.reversedDoneVehicleRenting(CAR_1);
        State next = stateEditor.makeState();

        // then: we rent a car, but in dropoff time
        assertEquals(TraverseMode.CAR, next.getNonTransitMode());
        assertEquals(CAR_1, next.getCurrentVehicle());
        assertEquals(0, next.distanceTraversedInCurrentVehicle, DELTA);
        assertEquals(state.time + request.routingDelays.getDropoffTime(CAR_1) * 1000, next.time);
    }
}
