package org.opentripplanner.routing.core;

import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.vertextype.IntersectionVertex;

/**
 * Abstract turn cost model provides various methods most implementations will use.
 *
 * @author avi
 */
public abstract class AbstractIntersectionTraversalCostModel implements IntersectionTraversalCostModel {

    /**
     * Factor by which absolute turn angles are divided to get turn costs for non-driving scenarios.
     */
    private static final double NON_DRIVING_TURN_COST_FACTOR = 1.0 / 20.0;

    private static final int MIN_RIGHT_TURN_ANGLE = 45;
    private static final int MAX_RIGHT_TURN_ANGLE = 135;
    private static final int MIN_LEFT_TURN_ANGLE = 225;
    private static final int MAX_LEFT_TURN_ANGLE = 315;

    /**
     * If true, cost turns as they would be in a country where driving occurs on the right; otherwise, cost them as they would be in a country where
     * driving occurs on the left.
     */
    private static final boolean DRIVE_ON_RIGHT = true;

    /**
     * Returns true if this angle represents a right turn.
     */
    protected boolean isRightTurn(int turnAngle) {
        return (turnAngle >= MIN_RIGHT_TURN_ANGLE && turnAngle < MAX_RIGHT_TURN_ANGLE);
    }

    /**
     * Returns true if this angle represents a left turn.
     */
    protected boolean isLeftTurn(int turnAngle) {
        return (turnAngle >= MIN_LEFT_TURN_ANGLE && turnAngle < MAX_LEFT_TURN_ANGLE);
    }

    /**
     * Computes the turn cost in seconds for non-driving traversal modes.
     * <p>
     * TODO(flamholz): this should probably account for whether there is a traffic light?
     */
    protected double computeNonDrivingTraversalCost(IntersectionVertex v, StreetEdge from, StreetEdge to,
                                                    float fromSpeed, float toSpeed) {
        int outAngle = to.getOutAngle();
        int inAngle = from.getInAngle();
        int turnCost = Math.abs(outAngle - inAngle);
        if (turnCost > 180) {
            turnCost = 360 - turnCost;
        }

        // NOTE: This makes the turn cost lower the faster you're going
        return (NON_DRIVING_TURN_COST_FACTOR * turnCost) / toSpeed;
    }

    /**
     * Calculates the turn angle from the incoming/outgoing edges and routing request.
     * <p>
     * Corrects for the side of the street they are driving on.
     */
    protected int calculateTurnAngle(StreetEdge from, StreetEdge to) {
        int angleOutOfIntersection = to.getInAngle();
        int angleIntoIntersection = from.getOutAngle();

        // Put out to the right of in; i.e. represent everything as one long right turn
        // Also ensures that turnAngle is always positive.
        if (angleOutOfIntersection < angleIntoIntersection) {
            angleOutOfIntersection += 360;
        }

        int turnAngle = angleOutOfIntersection - angleIntoIntersection;

        if (!DRIVE_ON_RIGHT) {
            turnAngle = 360 - turnAngle;
        }

        return turnAngle;
    }
}
