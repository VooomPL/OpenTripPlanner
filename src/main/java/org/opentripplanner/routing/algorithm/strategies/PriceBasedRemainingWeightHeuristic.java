package org.opentripplanner.routing.algorithm.strategies;

import org.opentripplanner.common.geometry.SphericalDistanceLibrary;
import org.opentripplanner.routing.algorithm.costs.CostFunction;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleDescription;
import org.opentripplanner.routing.core.vehicle_sharing.VehiclePricingPackage;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Vertex;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class PriceBasedRemainingWeightHeuristic implements RemainingWeightHeuristic {

    private static final double DEFAULT_PRICE_COST_WEIGHT = 1;
    private static final double DEFAULT_ORIGINAL_COST_WEIGHT = 0.01;

    private double lat;
    private double lon;
    private double priceCostWeight;
    private double originalCostWeight;
    private EuclideanRemainingWeightHeuristic originalHeuristic;

    public PriceBasedRemainingWeightHeuristic(Map<CostFunction.CostCategory, Double> costCategoryWeights){
        priceCostWeight = Optional.ofNullable(costCategoryWeights.get(CostFunction.CostCategory.PRICE_ASSOCIATED)).orElse(DEFAULT_PRICE_COST_WEIGHT);
        originalCostWeight = Optional.ofNullable(costCategoryWeights.get(CostFunction.CostCategory.ORIGINAL)).orElse(DEFAULT_ORIGINAL_COST_WEIGHT);
        originalHeuristic = new EuclideanRemainingWeightHeuristic();
    }

    @Override
    public void initialize(RoutingRequest request, long abortTime) {
        Vertex target = request.rctx.target;
        lat = target.getLat();
        lon = target.getLon();
        originalHeuristic.initialize(request, abortTime);
    }

    @Override
    public double estimateRemainingWeight(State s) {
        Vertex currentLocation = s.getVertex();
        double remainingDistance = SphericalDistanceLibrary.distance(currentLocation.getLat(), currentLocation.getLon(), lat, lon);

        double speed;
        int remainingTime;
        double estimatedFuturePrice = 0;
        Edge backEdge = s.getBackEdge();
        if (Objects.nonNull(s.getCurrentVehicle()) && Objects.nonNull(backEdge) && backEdge instanceof StreetEdge ) {
            speed = s.getCurrentVehicle().getMaxSpeedInMetersPerSecond((StreetEdge) backEdge);
            remainingTime = (int) (remainingDistance / speed);
            estimatedFuturePrice = chooseBestFuturePrice(s.getCurrentVehicle(),
                    s.getTimeTraversedInCurrentVehicleInSeconds(), s.getDistanceTraversedInCurrentVehicle(),
                    remainingTime, remainingDistance);
        }

        return priceCostWeight * estimatedFuturePrice + originalCostWeight * originalHeuristic.estimateRemainingWeight(s);
    }

    private double chooseBestFuturePrice(VehicleDescription vehicle,
                                             int previousTravelTimeInSeconds,
                                             double previousDistanceInMeters,
                                             int remainingTravelTimeInSeconds,
                                             double remainingDistanceInMeters) {
        VehiclePricingPackage evaluatedPackage = vehicle.getVehiclePricingPackage(0);
        BigDecimal chosenEstimatedTotalPrice = evaluatedPackage.computeTotalPrice(
                previousTravelTimeInSeconds + remainingTravelTimeInSeconds,
                (int) (previousDistanceInMeters + remainingDistanceInMeters));
        BigDecimal chosenEstimatedPreviousPrice = evaluatedPackage.computeTotalPrice(previousTravelTimeInSeconds, (int) previousDistanceInMeters);

        BigDecimal evaluatedTotalPrice;
        for (int i = 1; i < vehicle.getVehiclePricingPackages().size(); i++) {
            evaluatedPackage = vehicle.getVehiclePricingPackage(i);
            evaluatedTotalPrice = evaluatedPackage.computeTotalPrice(
                    previousTravelTimeInSeconds + remainingTravelTimeInSeconds,
                    (int) (previousDistanceInMeters + remainingDistanceInMeters));
            if (evaluatedTotalPrice.compareTo(chosenEstimatedTotalPrice) < 0) {
                chosenEstimatedTotalPrice = evaluatedTotalPrice;
                chosenEstimatedPreviousPrice = evaluatedPackage.computeTotalPrice(previousTravelTimeInSeconds, (int) previousDistanceInMeters);
            }
        }

        return chosenEstimatedTotalPrice.subtract(chosenEstimatedPreviousPrice).doubleValue();
    }

    @Override
    public void reset() {}

    @Override
    public void doSomeWork() {}

}
