package org.opentripplanner.routing.algorithm.strategies;

import com.google.common.collect.Iterables;
import org.opentripplanner.common.geometry.SphericalDistanceLibrary;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleDescription;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleType;
import org.opentripplanner.routing.edgetype.FreeEdge;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;


public class MultimodalHeuristic implements RemainingWeightHeuristic {
    private static Logger LOG = LoggerFactory.getLogger(MultimodalHeuristic.class);

    private double lat;
    private double lon;
    private RoutingRequest options;

    @Override
    public void initialize(RoutingRequest options, long abortTime) {
        RoutingRequest req = options;
        Vertex target = req.rctx.target;

        if (target.getDegreeIn() == 1) {
            Edge edge = Iterables.getOnlyElement(target.getIncoming());
            if (edge instanceof FreeEdge) {
                target = edge.getFromVertex();
            }
        }

        lat = target.getLat();
        lon = target.getLon();


        this.options = options;
    }


    private double estimateTime(VehicleType vehicleType, double distance) {
        double speed;

        if (vehicleType == VehicleType.CAR) {
            speed = options.carSpeed;
        } else if (vehicleType == VehicleType.BIKE) {
            speed = options.bikeSpeed;
        } else if (vehicleType == VehicleType.MOTORBIKE) {
            speed = 70D * 1000D / 3600D;
        } else {
            speed = options.walkSpeed;
        }

        return (distance / speed);

    }


    private double dropCurrentMode(VehicleDescription vehicleDescription) {
        if (vehicleDescription == null) {
            return 0;
        } else
            return options.routingDelays.getDropoffTime(vehicleDescription) * options.routingReluctances.getRentingReluctance();
    }

    private double currentModeReluctance(State s) {
        if (s.getCurrentVehicleType() == null) {
            return options.routingReluctances.getWalkReluctance();
        } else {
            return options.routingReluctances.getModeVehicleReluctance(s.getCurrentVehicleType(), null);
        }
    }

    private double oneTransferWeight(State s, VehicleType vehicleType, double distanceToTarget, double dropCurrentVehicleWeight) {
        double distanceToVehicle = findClosest(s.getVertex(), vehicleType);
        double distanceFromVehicle = distanceToTarget - distanceToVehicle;

        if (distanceFromVehicle < 0)
            return Double.MAX_VALUE;

        double weightToVehicle = distanceToVehicle * currentModeReluctance(s);
        double weightFromVehicle = distanceFromVehicle * options.routingReluctances.getModeVehicleReluctance(vehicleType, null);

        return weightFromVehicle + weightToVehicle + dropCurrentVehicleWeight;
    }

    private double twoTransferWeight(State s, VehicleType vehicleType1, VehicleType vehicleType2, double distanceToTarget, double dropCurrentVehicleWeight) {
        double distanceToVehicle1 = findClosest(s.getVertex(), vehicleType1);
        double distanceToVehicle2 = findClosest(s.getVertex(), vehicleType2);

        double distanceFromVehicle1 = distanceToVehicle2 - distanceToVehicle1;
        double distanceFromVehicle2 = distanceToTarget - distanceToVehicle2;
        if (distanceFromVehicle1 < 0 || distanceFromVehicle2 < 0)
            return Double.MAX_VALUE;
        double weightToVehicle1 = distanceToVehicle1 * currentModeReluctance(s);
        double weightToVehicle2 = distanceFromVehicle1 * options.routingReluctances.getModeVehicleReluctance(vehicleType1, null);
        double weightToTarget = distanceFromVehicle2 * options.routingReluctances.getModeVehicleReluctance(vehicleType2, null);

        return weightToTarget + weightToVehicle1 + weightToVehicle2 + dropCurrentVehicleWeight;
    }

    @Override
    public double estimateRemainingWeight(State s) {
        Vertex sv = s.getVertex();

        double distanceToTarget = SphericalDistanceLibrary.fastDistance(sv.getLat(), sv.getLon(), lat, lon);

        double dropCurrentVehicleWeight = s.getCurrentVehicleType() != null ? dropCurrentMode(s.getCurrentVehicle()) : 0;

        double w0 = options.routingReluctances.getWalkReluctance() * distanceToTarget / options.carSpeed + dropCurrentVehicleWeight;
        //We can go straight for the target
        double w1 = estimateTime(s.getCurrentVehicleType(), distanceToTarget) * currentModeReluctance(s) + dropCurrentVehicleWeight;

        //We can go to the nearest kickscooter
        double w2 = oneTransferWeight(s, VehicleType.KICKSCOOTER, distanceToTarget, dropCurrentVehicleWeight);
        //We can take the nearest car
        double w3 = oneTransferWeight(s, VehicleType.CAR, distanceToTarget, dropCurrentVehicleWeight);
        //We can take the nearest motorbike
        double w4 = oneTransferWeight(s, VehicleType.MOTORBIKE, distanceToTarget, dropCurrentVehicleWeight);


        double w5 = twoTransferWeight(s, VehicleType.KICKSCOOTER, VehicleType.CAR, distanceToTarget, dropCurrentVehicleWeight);
        double w6 = twoTransferWeight(s, VehicleType.KICKSCOOTER, VehicleType.MOTORBIKE, distanceToTarget, dropCurrentVehicleWeight);
        double w7 = twoTransferWeight(s, VehicleType.MOTORBIKE, VehicleType.CAR, distanceToTarget, dropCurrentVehicleWeight);

//        return Collections.min(Arrays.asList(w1, w2, w3, w5));
        return 0.0 * w0 + 1 * Collections.min(Arrays.asList(w1, w2, w3, w4, w5, w6, w6, w7));
    }

    @Override
    public void reset() {

    }

    @Override
    public void doSomeWork() {

    }


    private double findClosest(Vertex v, VehicleType type) {
        if (type == null) {
            return v.closestStop;
        } else if (type == VehicleType.CAR) {
            return v.closestCar;
        } else if (type == VehicleType.KICKSCOOTER) {
            return v.closestKickscooter;
        } else {
            return v.closestMotorbike;
        }
    }

}
