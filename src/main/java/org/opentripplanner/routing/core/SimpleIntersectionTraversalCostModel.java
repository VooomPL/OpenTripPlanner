package org.opentripplanner.routing.core;

import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.vertextype.IntersectionVertex;

import java.io.Serializable;

public class SimpleIntersectionTraversalCostModel extends AbstractIntersectionTraversalCostModel implements Serializable {

    // Model parameters are here. //
    // Constants for when there is a traffic light.

    /**
     * Expected time it takes to make a right at a light.
     */
    private static final double EXPECTED_RIGHT_AT_LIGHT_TIME_SEC = 15.0;

    /**
     * Expected time it takes to continue straight at a light.
     */
    private static final double EXPECTED_STRAIGHT_AT_LIGHT_TIME_SEC = 15.0;

    /**
     * Expected time it takes to turn left at a light.
     */
    private static final double EXPECTED_LEFT_AT_LIGHT_TIME_SEC = 15.0;

    // Constants for when there is no traffic light

    /**
     * Expected time it takes to make a right without a stop light.
     */
    private static final double EXPECTED_RIGHT_NO_LIGHT_TIME_SEC = 8.0;

    /**
     * Expected time it takes to continue straight without a stop light.
     */
    private static final double EXPECTED_STRAIGHT_NO_LIGHT_TIME_SEC = 5.0;

    /**
     * Expected time it takes to turn left without a stop light.
     */
    private static final double EXPECTED_LEFT_NO_LIGHT_TIME_SEC = 8.0;

    private static final double HIGHWAY_SPEED = 25.0;

    @Override
    public double computeTraversalCost(IntersectionVertex v, StreetEdge from, StreetEdge to, TraverseMode mode,
                                       RoutingRequest options, float fromSpeed, float toSpeed) {

        // If the vertex is free-flowing then (by definition) there is no cost to traverse it.
        if (v.inferredFreeFlowing()) {
            return 0;
        }

        // Non-driving cases are much simpler. Handled generically in the base class.
        if (!mode.isDriving()) {
            return computeNonDrivingTraversalCost(v, from, to, fromSpeed, toSpeed);
        }

        double turnCost = 0;

        int turnAngle = calculateTurnAngle(from, to);
        if (v.trafficLight) {
            // Use constants that apply when there are stop lights.
            if (isRightTurn(turnAngle)) {
                turnCost = EXPECTED_RIGHT_AT_LIGHT_TIME_SEC;
            } else if (isLeftTurn(turnAngle)) {
                turnCost = EXPECTED_LEFT_AT_LIGHT_TIME_SEC;
            } else {
                turnCost = EXPECTED_STRAIGHT_AT_LIGHT_TIME_SEC;
            }
        } else {

            //assume highway vertex
            if (from.getMaxStreetTraverseSpeed() > HIGHWAY_SPEED && to.getMaxStreetTraverseSpeed() > HIGHWAY_SPEED) {
                return 0;
            }

            // Use constants that apply when no stop lights.
            if (isRightTurn(turnAngle)) {
                turnCost = EXPECTED_RIGHT_NO_LIGHT_TIME_SEC;
            } else if (isLeftTurn(turnAngle)) {
                turnCost = EXPECTED_LEFT_NO_LIGHT_TIME_SEC;
            } else {
                turnCost = EXPECTED_STRAIGHT_NO_LIGHT_TIME_SEC;
            }
        }

        return turnCost;
    }
}
