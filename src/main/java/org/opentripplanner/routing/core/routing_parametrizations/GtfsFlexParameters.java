package org.opentripplanner.routing.core.routing_parametrizations;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class GtfsFlexParameters implements Cloneable {

    /**
     * Extra penalty added for flag-stop boarding/alighting. This parameter only applies to
     * GTFS-Flex routing, which must be explicitly turned on via the useFlexService parameter
     * in router-config.json.
     * <p>
     * In GTFS-Flex, a flag stop is a point at which a vehicle is boarded or alighted which is not
     * a defined stop, e.g. the bus is flagged down in between stops. This parameter is an
     * additional cost added when a board/alight occurs at a flag stop. Increasing this parameter
     * increases the cost of using a flag stop relative to a regular scheduled stop.
     */
    private int flagStopExtraPenalty = 90;

    /**
     * Extra penalty added for deviated-route boarding/alighting. This parameter only applies to
     * GTFS-Flex routing, which must be explicitly turned on via the useFlexService parameter
     * in router-config.json.
     * <p>
     * In GTFS-Flex, deviated-route service is when a vehicle can deviate a certain distance
     * (or within a certain area) in order to drop off or pick up a passenger. This parameter is an
     * additional cost added when a board/alight occurs before/after a deviation. Increasing this
     * parameter increases the cost of using deviated-route service relative to fixed-route.
     */
    private int deviatedRouteExtraPenalty = 180;

    /**
     * Reluctance for call-n-ride. This parameter only applies to GTFS-Flex routing, which must be
     * explicitly turned on via the useFlexService parameter in router-config.json.
     * <p>
     * Call-and-ride service is when a vehicle picks up and drops off a passenger at their origin
     * and destination, without regard to a fixed route. In the GTFS-Flex data standard, call-and-
     * ride service is defined analogously to deviated-route service, but with more permissive
     * parameters. Depending on the particular origin and destination and the size of the area in
     * which a route can deviate, a single route could be used for both deviated-route and call-
     * and-ride service. This parameter is multiplied with the time on board call-and-ride in order to
     * increase the cost of call-and-ride's use relative to normal transit.
     */
    private double callAndRideReluctance = 2.0;

    /**
     * Total time which can be spent on a call-n-ride leg. This parameter only applies to GTFS-Flex
     * routing, which must be explicitly turned on via the useFlexService parameter in
     * router-config.json.
     * <p>
     * "Trip-banning" as a method of obtaining different itinerary results does not work for call-
     * and-ride service: the same trip can be used in different ways, for example to drop off a
     * passenger at different transfer points. Thus, rather than trip-banning, after each itinerary
     * is found, flexMaxCallAndRideSeconds is reduced in order to obtain different itineraries. The
     * new value of the parameter to calculated according to the following formula:
     * min(duration - options.flexReduceCallAndRideSeconds, duration * flexReduceCallAndRideRatio)
     */
    private int maxCallAndRideSeconds = Integer.MAX_VALUE;

    /**
     * Control the reduction of call-and-ride time. This parameter only applies to GTFS-Flex
     * routing, which must be explicitly turned on via the useFlexService parameter in
     * router-config.json.
     * <p>
     * Seconds to reduce flexMaxCallAndRideSeconds after a complete call-n-ride itinerary. The
     * rationale for this parameter is given in the docs for flexMaxCallAndRideSeconds.
     */
    private int reduceCallAndRideSeconds = 15 * 60;

    /**
     * Control the reduction of call-and-ride time. This parameter only applies to GTFS-Flex
     * routing, which must be explicitly turned on via the useFlexService parameter in
     * router-config.json.
     * <p>
     * Percentage to reduce flexMaxCallAndRideSeconds after a complete call-n-ride itinerary. The
     * rationale for this parameter is given in the docs for flexMaxCallAndRideSeconds.
     */
    private double reduceCallAndRideRatio = 0.5;

    /**
     * Control the size of flag-stop buffer returned in API response. This parameter only applies
     * to GTFS-Flex routing, which must be explicitly turned on via the useFlexService parameter in
     * router-config.json.
     * <p>
     * This allows the UI to specify the length in meters of a segment around flag stops it wants
     * to display, as an indication to the user that the vehicle may be flagged down anywhere on
     * the segment. The backend will supply such a cropped geometry in its response
     * (`Place.flagStopArea`). The segment will be up to flexFlagStopBufferSize meters ahead or
     * behind the board/alight location. The actual length may be less if the board/alight location
     * is near the beginning or end of a route.
     */
    private double flagStopBufferSize;

    /**
     * Whether to use reservation-based services. This parameter only applies to GTFS-Flex
     * routing, which must be explicitly turned on via the useFlexService parameter in
     * router-config.json.
     * <p>
     * In GTFS-Flex, some trips may be defined as "reservation services," which indicates that
     * they require a reservation in order to be used. Such services will only be used if this
     * parameter is true.
     */
    private boolean useReservationServices = true;

    /**
     * Whether to use eligibility-based services. This parameter only applies to GTFS-Flex
     * routing, which must be explicitly turned on via the useFlexService parameter in
     * router-config.json.
     * <p>
     * In GTFS-Flex, some trips may be defined as "eligibility services," which indicates that
     * they require customers to meet a certain set of requirements in order to be used. Such
     * services will only be used if this parameter is true.
     */
    private boolean useEligibilityServices = true;

    /**
     * Whether to ignore DRT time limits. This parameter only applies to GTFS-Flex routing, which
     * must be explicitly turned on via the useFlexService parameter in router-config.json.
     * <p>
     * In GTFS-Flex, deviated-route and call-and-ride service can define a trip-level parameter
     * `drt_advance_book_min`, which determines how far in advance the flexible segment must be
     * scheduled. If `flexIgnoreDrtAdvanceBookMin = false`, OTP will only provide itineraries which
     * are feasible based on that constraint. For example, if the current time is 1:00pm and a
     * particular service must be scheduled one hour in advance, the earliest time the service
     * is usable is 2:00pm.
     */
    private boolean ignoreDrtAdvanceBookMin = false;

    /**
     * Minimum length in meters of partial hop edges. This parameter only applies to GTFS-Flex
     * routing, which must be explicitly turned on via the useFlexService parameter in router-
     * config.json.
     * <p>
     * Flag stop and deviated-route service require creating partial PatternHops from points along
     * the route to a scheduled stop. This parameter provides a minimum length of such partial
     * hops, in order to reduce the amount of hops created when they redundant with regular
     * service.
     */
    private int minPartialHopLength = 400;

    /**
     * Keep track of epoch time the request was created by OTP. This is currently only used by the
     * GTFS-Flex implementation.
     * <p>
     * In GTFS-Flex, deviated-route and call-and-ride service can define a trip-level parameter
     * `drt_advance_book_min`, which determines how far in advance the flexible segment must be
     * scheduled. If `flexIgnoreDrtAdvanceBookMin = false`, OTP will only provide itineraries which
     * are feasible based on that constraint. For example, if the current time is 1:00pm and a
     * particular service must be scheduled one hour in advance, the earliest time the service
     * is usable is 2:00pm.
     */
    private long clockTimeSec;

    public void resetClockTime() {
        clockTimeSec = System.currentTimeMillis() / 1000;
    }

    public GtfsFlexParameters clone() {
        try {
            return (GtfsFlexParameters) super.clone();
        } catch (CloneNotSupportedException e) {
            /* this will never happen since our super is the cloneable object */
            throw new RuntimeException(e);
        }
    }
}
