package org.opentripplanner.routing.algorithm.strategies;

import com.google.common.collect.Iterables;
import org.opentripplanner.common.geometry.SphericalDistanceLibrary;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleType;
import org.opentripplanner.routing.edgetype.FreeEdge;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class MultimodalHeuristic2 implements RemainingWeightHeuristic {
    private static Logger LOG = LoggerFactory.getLogger(MultimodalHeuristic2.class);

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


    private double transferTrip(List<TraversePackage> traversePackageList, double distanceToTarget, Vertex vertex) {
        double weight = 0;
        TraversePackage prevTraversePackage = null;

        for (TraversePackage traversePackage : traversePackageList) {
            if (prevTraversePackage == null) {
                prevTraversePackage = traversePackage;
                continue;
            }
            double distance = traversePackage.findClosest(vertex) - prevTraversePackage.findClosest(vertex);
            if (distance < 0)
                return Double.MAX_VALUE;

            weight += (prevTraversePackage.getDropoffTime() + prevTraversePackage.getRentingTime()) * options.routingReluctances.getRentingReluctance();

            weight += prevTraversePackage.reluctance() * (distance) / prevTraversePackage.getSpeed();
            prevTraversePackage = traversePackage;
        }

        double remainingDistance = distanceToTarget - prevTraversePackage.findClosest(vertex);
        if (remainingDistance < 0)
            return Double.MAX_VALUE;

//        weight += (prevTraversePackage.getDropoffTime() + prevTraversePackage.getRentingTime()) * options.routingReluctances.getRentingReluctance();

        weight += prevTraversePackage.reluctance() * (remainingDistance) / prevTraversePackage.getSpeed();


        return weight;
    }


    @Override
    public double estimateRemainingWeight(State s) {
        Vertex sv = s.getVertex();

        CarPackage carPackage = new CarPackage();
        KickscooterPackage kickscooterPackage = new KickscooterPackage();
        MotorbikePackage motorbikePackage = new MotorbikePackage();
        PublicTransportPackage publicTransportPackage = new PublicTransportPackage();
        CurrentModePackage currentModePackage = new CurrentModePackage(s, options);

        double distanceToTarget = SphericalDistanceLibrary.fastDistance(sv.getLat(), sv.getLon(), lat, lon);

//      To jest oryginalny, euklidesowy szacunek
        double w0 = options.routingReluctances.getWalkReluctance() * distanceToTarget / options.carSpeed;
//      double w0 = ;

//      We can go straight for the target
        double w1 = transferTrip(Collections.singletonList(currentModePackage), distanceToTarget, sv);

        //We can go to the nearest kickscooter
        double w2 = transferTrip(Arrays.asList(currentModePackage, carPackage), distanceToTarget, sv);
        //We can take the nearest car
        double w3 = transferTrip(Arrays.asList(currentModePackage, kickscooterPackage), distanceToTarget, sv);
        //We can take the nearest motorbike
        double w4 = transferTrip(Arrays.asList(currentModePackage, motorbikePackage), distanceToTarget, sv);
//        double w5 = transferTrip(Arrays.asList(currentModePackage, publicTransportPackage), distanceToTarget, sv);


//        double w6 = transferTrip(Arrays.asList(currentModePackage, kickscooterPackage, carPackage), distanceToTarget, sv);
//        double w7 = transferTrip(Arrays.asList(currentModePackage, kickscooterPackage, publicTransportPackage), distanceToTarget, sv);
//        double w8 = transferTrip(Arrays.asList(currentModePackage, motorbikePackage, carPackage), distanceToTarget, sv);
//        double w9 = transferTrip(Arrays.asList(currentModePackage, kickscooterPackage, motorbikePackage), distanceToTarget, sv);
//        double w10 = transferTrip(Arrays.asList(currentModePackage, motorbikePackage, publicTransportPackage), distanceToTarget, sv);


        return 0 * w0 + 1.0 * Collections.min(Arrays.asList(w1, w2, w3, w4));// w5));//, w6, w6, w7, w8, w9, w10));
//        return 0* w0 + 1.0 * Collections.min(Arrays.asList(w1, w2, w3, w4, w5));//, w6, w6, w7, w8, w9, w10));
//        return 0.5 * w0 + 0.5* Collections.min(Arrays.asList(w1, w2, w3, w4, w5, w6, w6, w7, w8, w9, w10));
    }

    @Override
    public void reset() {

    }

    @Override
    public void doSomeWork() {

    }


    private abstract class TraversePackage {
        abstract double findClosest(Vertex vertex);

        abstract double getSpeed();

        abstract double getDropoffTime();

        abstract double getRentingTime();

        abstract double reluctance();
    }

    private class CurrentModePackage extends TraversePackage {
        State s;
        RoutingRequest options;

        public CurrentModePackage(State s, RoutingRequest options) {
            this.s = s;
            this.options = options;
        }

        @Override
        double findClosest(Vertex vertex) {
            return 0;
        }

        @Override
        double getSpeed() {
            if (s.getCurrentVehicleType() == VehicleType.CAR) {
                return options.carSpeed;
            } else if (s.getCurrentVehicleType() == VehicleType.BIKE) {
                return options.bikeSpeed;
            } else if (s.getCurrentVehicleType() == VehicleType.MOTORBIKE) {
                return 70D * 1000D / 3600D;
            } else {
                return options.walkSpeed;
            }
        }

        @Override
        double getDropoffTime() {
            if (s.getCurrentVehicle() == null) {
                return 0;
            } else {
                return options.routingDelays.getDropoffTime(s.getCurrentVehicle());
            }
        }

        @Override
        double getRentingTime() {
            return 0;
        }

        @Override
        double reluctance() {
            return 1;
        }
    }

    private class PublicTransportPackage extends TraversePackage {

        @Override
        double findClosest(Vertex vertex) {
            return vertex.closestStop;
        }

        @Override
        double getSpeed() {
            return 70D / 3.6;
        }

        @Override
        double getDropoffTime() {
            return 0;
        }

        @Override
        double getRentingTime() {
            return 0;
        }

        @Override
        double reluctance() {
            return 1;
        }
    }

    private class CarPackage extends TraversePackage {

        @Override
        double findClosest(Vertex vertex) {
            return 0d;
        }

        @Override
        double getSpeed() {
            return options.carSpeed;
        }

        @Override
        double getDropoffTime() {
            return options.routingDelays.getCarDropoffTime();
        }

        @Override
        double getRentingTime() {
            return options.routingDelays.getCarRentingTime();
        }

        @Override
        double reluctance() {
            return 1;
        }
    }

    private class KickscooterPackage extends TraversePackage {

        @Override
        double findClosest(Vertex vertex) {
            return 0d;
        }

        @Override
        double getSpeed() {
            return options.bikeSpeed;
        }

        @Override
        double getDropoffTime() {
            return options.routingDelays.getKickScooterDropoffTime();
        }

        @Override
        double getRentingTime() {
            return options.routingDelays.getKickScooterRentingTime();
        }

        @Override
        double reluctance() {
            return 2.5;
        }
    }

    private class MotorbikePackage extends TraversePackage {

        @Override
        double findClosest(Vertex vertex) {
            return 0d;
        }

        @Override
        double getSpeed() {
            return 70D / 3.6;
        }

        @Override
        double getDropoffTime() {
            return options.routingDelays.getMotorbikeDropoffTime();
        }

        @Override
        double getRentingTime() {
            return options.routingDelays.getMotorbikeRentingTime();
        }

        @Override
        double reluctance() {
            return 1;
        }
    }
}
