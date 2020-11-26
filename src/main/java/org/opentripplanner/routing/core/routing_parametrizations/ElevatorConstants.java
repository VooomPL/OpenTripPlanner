package org.opentripplanner.routing.core.routing_parametrizations;

public class ElevatorConstants {
    /**
     * How long does it take to get an elevator, on average (actually, it probably should be a bit *more* than average, to prevent optimistic trips)?
     * Setting it to "seems like forever," while accurate, will probably prevent OTP from working correctly.
     */
    // TODO: how long does it /really/ take to get an elevator?
    public static final int ELEVATOR_BOARD_TIME = 90;

    /**
     * What is the cost of boarding an elevator?
     */
    public static final int ELEVATOR_BOARD_COST = 90;

    /**
     * How long does it take to advance one floor on an elevator?
     */
    public static final int ELEVATOR_HOP_TIME = 20;

    /**
     * What is the cost of travelling one floor on an elevator?
     */
    public static final int ELEVATOR_HOP_COST = 20;

    // it is assumed that getting off an elevator is completely free
}
