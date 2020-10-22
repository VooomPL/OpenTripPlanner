package org.opentripplanner.routing.algorithm.strategies;

import org.opentripplanner.common.geometry.SphericalDistanceLibrary;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleDescription;
import org.opentripplanner.routing.core.vehicle_sharing.VehiclePricingPackage;
import org.opentripplanner.routing.graph.Vertex;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class PriceBasedRemainingWeightHeuristic implements RemainingWeightHeuristic {

    private double lat;
    private double lon;
    private double walkReluctance;
    private double walkSpeed;

    @Override
    public void initialize(RoutingRequest request, long abortTime) {
        Vertex target = request.rctx.target;
        lat = target.getLat();
        lon = target.getLon();
        walkReluctance = request.routingReluctances.getModeVehicleReluctance(null, TraverseMode.WALK);
        walkSpeed = request.walkSpeed;
    }

    @Override
    public double estimateRemainingWeight(State s) {
        Vertex currentLocation = s.getVertex();
        double remainingDistance = SphericalDistanceLibrary.distance(currentLocation.getLat(), currentLocation.getLon(), lat, lon);

        double speed;
        int remainingTime;
        double estimatedFuturePrice;
        if (Objects.isNull(s.getCurrentVehicle())) {
            TraverseMode traverseMode = Optional.ofNullable(s.getBackMode()).orElse(TraverseMode.WALK);
            remainingTime = (int) (remainingDistance / (traverseMode == TraverseMode.WALK ? walkSpeed : RoutingRequest.getDefaultTransitSpeed(traverseMode)));
            estimatedFuturePrice = BigDecimal.valueOf(remainingTime)
                    .divide(BigDecimal.valueOf(TimeUnit.MINUTES.toSeconds(1)), RoundingMode.UP)
                    .multiply(BigDecimal.valueOf(walkReluctance)).doubleValue();
        } else {
            speed = s.getCurrentVehicle().getMaxSpeedInMetersPerSecond(null);
            remainingTime = (int) (remainingDistance / speed);
            estimatedFuturePrice = chooseBestFuturePrice(s.getCurrentVehicle(),
                    s.getTimeTraversedInCurrentVehicleInSeconds(), s.getDistanceTraversedInCurrentVehicle(),
                    remainingTime, remainingDistance);
        }

        return estimatedFuturePrice;
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
