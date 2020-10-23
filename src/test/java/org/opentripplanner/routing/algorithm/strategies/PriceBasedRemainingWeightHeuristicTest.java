package org.opentripplanner.routing.algorithm.strategies;

import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.opentripplanner.common.geometry.GeometryUtils;
import org.opentripplanner.routing.algorithm.costs.CostFunction;
import org.opentripplanner.routing.algorithm.profile.OptimizationProfile;
import org.opentripplanner.routing.core.*;
import org.opentripplanner.routing.core.vehicle_sharing.*;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.edgetype.StreetTraversalPermission;
import org.opentripplanner.routing.edgetype.rentedgetype.RentVehicleEdge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.spt.DominanceFunction;
import org.opentripplanner.routing.vertextype.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class PriceBasedRemainingWeightHeuristicTest {

    private RoutingRequest request;
    private List<VehiclePricingPackage> availablePricingPackages;
    private TemporaryRentVehicleVertex rentVehicleVertex;
    private SampleVertex destinationVertex;
    private StreetEdge streetEdge;
    private RentVehicleEdge rentEdge;

    @Before
    public void setUp() {
        Graph graph = new Graph();
        configurePricingPackages();
        rentVehicleVertex = new TemporaryRentVehicleVertex("id", new Coordinate(1, 2), "name");
        destinationVertex = new SampleVertex(graph, new Coordinate(1, 2.05));
        streetEdge = edge(rentVehicleVertex, new IntersectionVertex(graph, "Flemminggatan", 1, 2.03), 3336, StreetTraversalPermission.ALL);
        rentEdge = new RentVehicleEdge(rentVehicleVertex, null);

        request = new RoutingRequest();
        request.setRoutingContext(new Graph(), rentVehicleVertex, destinationVertex);
        request.routingReluctances.setWalkReluctance(0.2);
        configurePriceBasedOptimizationProfile();
    }

    private StreetEdge edge(StreetVertex vA, StreetVertex vB, double length,
                            StreetTraversalPermission perm) {
        String labelA = vA.getLabel();
        String labelB = vB.getLabel();
        String name = String.format("%s_%s", labelA, labelB);
        Coordinate[] coords = new Coordinate[2];
        coords[0] = vA.getCoordinate();
        coords[1] = vB.getCoordinate();
        LineString geom = GeometryUtils.getGeometryFactory().createLineString(coords);

        return new StreetEdge(vA, vB, geom, name, length, perm, false);
    }

    private void configurePriceBasedOptimizationProfile() {
        request.setOptimizationProfile(new OptimizationProfile() {
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
        availablePricingPackages = new ArrayList<>();

        VehiclePricingPackage pricingPackage = new VehiclePricingPackage();
        pricingPackage.setPackagePrice(BigDecimal.valueOf(9.89));
        pricingPackage.setPackageTimeLimitInSeconds(180);
        pricingPackage.setMaxRentingPrice(BigDecimal.valueOf(199));
        pricingPackage.setDrivingPricePerTimeTickInPackageExceeded(BigDecimal.valueOf(2.0));
        pricingPackage.setKilometerPrice(BigDecimal.valueOf(0.8));
        pricingPackage.setMinRentingPrice(BigDecimal.valueOf(13));
        availablePricingPackages.add(pricingPackage);

        pricingPackage = new VehiclePricingPackage();
        pricingPackage.setPackagePrice(BigDecimal.valueOf(9.99));
        pricingPackage.setPackageTimeLimitInSeconds(180);
        pricingPackage.setMaxRentingPrice(BigDecimal.valueOf(199));
        pricingPackage.setDrivingPricePerTimeTickInPackageExceeded(BigDecimal.valueOf(1.29));
        pricingPackage.setKilometerPrice(BigDecimal.valueOf(0.1));
        pricingPackage.setMinRentingPrice(BigDecimal.valueOf(15));
        availablePricingPackages.add(pricingPackage);

        pricingPackage = new VehiclePricingPackage();
        pricingPackage.setPackagePrice(BigDecimal.valueOf(9.99));
        pricingPackage.setPackageTimeLimitInSeconds(180);
        pricingPackage.setMaxRentingPrice(BigDecimal.valueOf(199));
        pricingPackage.setDrivingPricePerTimeTickInPackageExceeded(BigDecimal.valueOf(2.0));
        pricingPackage.setKilometerPrice(BigDecimal.valueOf(0.9));
        pricingPackage.setMinRentingPrice(BigDecimal.valueOf(13));
        availablePricingPackages.add(pricingPackage);

    }

    @Test
    public void shouldReturnDifferentFuturePricingPackageWeightThanUsedByState() {
        // given
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(13, "Innogy"), 60000.0, availablePricingPackages.get(0));
        car.getVehiclePricingPackages().add(availablePricingPackages.get(1));

        Map<CostFunction.CostCategory, Double> costWeights = new HashMap<>();
        costWeights.put(CostFunction.CostCategory.PRICE_ASSOCIATED, 1.0);
        costWeights.put(CostFunction.CostCategory.ORIGINAL, 0.0);
        RemainingWeightHeuristic heuristic = new PriceBasedRemainingWeightHeuristic(costWeights);
        heuristic.initialize(request, 0);

        // when
        State s0 = new State(request);
        new State(rentVehicleVertex, null, 0,0, request);
        StateEditor stateEditor = s0.edit(rentEdge);
        stateEditor.beginVehicleRenting(car);
        State s1 = stateEditor.makeState();
        State s2 = streetEdge.traverse(s1);

        // then
        assertEquals(0, s1.getActivePackageIndex());
        assertEquals(0, heuristic.estimateRemainingWeight(s2), 0);
    }

    @Test
    public void shouldReturnSameFuturePricingPackageWeightAsUsedByState() {
        // given
        CarDescription car = new CarDescription("1", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(13, "Innogy"), 60000.0, availablePricingPackages.get(0));
        car.getVehiclePricingPackages().add(availablePricingPackages.get(2));

        Map<CostFunction.CostCategory, Double> costWeights = new HashMap<>();
        costWeights.put(CostFunction.CostCategory.PRICE_ASSOCIATED, 1.0);
        costWeights.put(CostFunction.CostCategory.ORIGINAL, 0.0);
        RemainingWeightHeuristic heuristic = new PriceBasedRemainingWeightHeuristic(costWeights);
        heuristic.initialize(request, 0);

        // when
        State s0 = new State(request);
        new State(rentVehicleVertex, null, 0,0, request);
        StateEditor stateEditor = s0.edit(rentEdge);
        stateEditor.beginVehicleRenting(car);
        State s1 = stateEditor.makeState();
        State s2 = streetEdge.traverse(s1);

        // then
        assertEquals(0, s1.getActivePackageIndex());
        assertEquals(3.6, heuristic.estimateRemainingWeight(s2), 0);
    }

    @Test
    public void shouldOnlyReturnWalkingAssociatedWeight() {
        Map<CostFunction.CostCategory, Double> costWeights = new HashMap<>();
        costWeights.put(CostFunction.CostCategory.PRICE_ASSOCIATED, 1.0);
        costWeights.put(CostFunction.CostCategory.ORIGINAL, 0.0);
        RemainingWeightHeuristic heuristic = new PriceBasedRemainingWeightHeuristic(costWeights);
        heuristic.initialize(request, 0);

        // when
        State s0 = new State(request);
        new State(rentVehicleVertex, null, 0,0, request);
        State s2 = streetEdge.traverse(s0);

        // then
        assertEquals(0, heuristic.estimateRemainingWeight(s2), 0);
    }

}
