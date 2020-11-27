package org.opentripplanner.routing.algorithm.strategies;

import com.google.common.annotations.VisibleForTesting;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.vehicle_sharing.*;
import org.opentripplanner.routing.edgetype.FreeEdge;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Vertex;

import java.util.Optional;
import java.util.Set;

import static java.lang.Double.min;
import static org.opentripplanner.common.geometry.SphericalDistanceLibrary.fastDistance;

/**
 * Estimate remaining weight by multiplying euclidean distance to destination by lowest multiplier
 * for all possible traverse modes. Multiplier is just reluctance divided by speed.
 */
public class SimpleEuclideanRWH implements RemainingWeightHeuristic {

    private double lat;
    private double lon;
    private double bestMultiplier;

    @Override
    public void initialize(RoutingRequest options, long abortTime) {
        calculateDestination(options.rctx.target);
        calculateBestMultiplier(options);
    }

    private void calculateDestination(Vertex target) {
        if (target.getDegreeIn() == 1) {
            Optional<Edge> edge = target.getIncoming().stream().findFirst().filter(FreeEdge.class::isInstance);
            if (edge.isPresent()) {
                lat = edge.get().getFromVertex().getLat();
                lon = edge.get().getFromVertex().getLon();
                return;
            }
        }
        lat = target.getLat();
        lon = target.getLon();
    }

    private void calculateBestMultiplier(RoutingRequest options) {
        bestMultiplier = options.routingReluctances.getWalkReluctance() / options.walkSpeed;
        // max speed of transit in not higher than max speed of a car, and transit does not have a reluctance
        if (options.modes.isTransit()) {
            bestMultiplier = min(bestMultiplier, 1.0 / options.carSpeed);
        }
        if (options.rentingAllowed) {
            calculateBestMultiplierWithRentableVehicles(options);
        } else {
            calculateBestMultiplierWithOwnVehicles(options);
        }
    }

    private void calculateBestMultiplierWithRentableVehicles(RoutingRequest options) {
        Set<VehicleType> vehicleTypesAllowed = options.vehicleValidator.getVehicleTypesAllowed();
        if (vehicleTypesAllowed.contains(VehicleType.CAR)) {
            bestMultiplier = min(bestMultiplier, options.routingReluctances.getCarReluctance() / CarDescription.getMaxPossibleSpeed());
        }
        if (vehicleTypesAllowed.contains(VehicleType.MOTORBIKE)) {
            bestMultiplier = min(bestMultiplier, options.routingReluctances.getMotorbikeReluctance() / MotorbikeDescription.getMaxPossibleSpeed());
        }
        if (vehicleTypesAllowed.contains(VehicleType.KICKSCOOTER)) {
            bestMultiplier = min(bestMultiplier, options.routingReluctances.getKickScooterReluctance() / KickScooterDescription.getMaxPossibleSpeed());
        }
        if (vehicleTypesAllowed.contains(VehicleType.BIKE)) {
            bestMultiplier = min(bestMultiplier, options.routingReluctances.getBicycleReluctance() / BikeDescription.getMaxPossibleSpeed());
        }
    }

    private void calculateBestMultiplierWithOwnVehicles(RoutingRequest options) {
        if (options.modes.getCar()) {
            bestMultiplier = min(bestMultiplier, options.routingReluctances.getCarReluctance() / options.carSpeed);
        }
        if (options.modes.getBicycle()) {
            bestMultiplier = min(bestMultiplier, options.routingReluctances.getBicycleReluctance() / options.bikeSpeed);
        }
    }

    @Override
    public double estimateRemainingWeight(State s) {
        Vertex sv = s.getVertex();
        return bestMultiplier * fastDistance(sv.getLat(), sv.getLon(), lat, lon);
    }

    @Override
    public void reset() {
    }

    @Override
    public void doSomeWork() {
    }

    @VisibleForTesting
    public double getBestMultiplier() {
        return bestMultiplier;
    }
}
