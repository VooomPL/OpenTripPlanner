package org.opentripplanner.api.common;

import org.opentripplanner.api.parameter.QualifiedModeSet;
import org.opentripplanner.model.FeedScopedId;
import org.opentripplanner.routing.algorithm.costs.CostFunction;
import org.opentripplanner.routing.algorithm.profile.OptimizationProfileFactory;
import org.opentripplanner.routing.core.OptimizeType;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.routing_parametrizations.RoutingDelays;
import org.opentripplanner.routing.core.routing_parametrizations.RoutingReluctances;
import org.opentripplanner.routing.core.vehicle_sharing.*;
import org.opentripplanner.routing.request.BannedStopSet;
import org.opentripplanner.standalone.OTPServer;
import org.opentripplanner.standalone.Router;
import org.opentripplanner.util.ResourceBundleSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.util.*;

/**
 * This class defines all the JAX-RS query parameters for a path search as fields, allowing them to
 * be inherited by other REST resource classes (the trip planner and the Analyst WMS or tile
 * resource). They will be properly included in API docs generated by Enunciate. This implies that
 * the concrete REST resource subclasses will be request-scoped rather than singleton-scoped.
 * <p>
 * All defaults should be specified in the RoutingRequest, NOT as annotations on the query parameters.
 * JSON router configuration can then overwrite those built-in defaults, and only the fields of the resulting prototype
 * routing request for which query parameters are found are overwritten here. This establishes a priority chain:
 * RoutingRequest field initializers, then JSON router config, then query parameters.
 *
 * @author abyrd
 */
public abstract class RoutingResource {

    private static final Logger LOG = LoggerFactory.getLogger(RoutingResource.class);

    /**
     * The routerId selects between several graphs on the same server. The routerId is pulled from
     * the path, not the query parameters. However, the class RoutingResource is not annotated with
     * a path because we don't want it to be instantiated as an endpoint. Instead, the {routerId}
     * path parameter should be included in the path annotations of all its subclasses.
     */
    @PathParam("routerId")
    public String routerId;

    /**
     * The start location -- either latitude, longitude pair in degrees or a Vertex
     * label. For example, <code>40.714476,-74.005966</code> or
     * <code>mtanyctsubway_A27_S</code>.
     */
    @QueryParam("fromPlace")
    protected String fromPlace;

    /**
     * The end location (see fromPlace for format).
     */
    @QueryParam("toPlace")
    protected String toPlace;

    /**
     * An ordered list of intermediate locations to be visited (see the fromPlace for format). Parameter can be specified multiple times.
     */
    @QueryParam("intermediatePlaces")
    protected List<String> intermediatePlaces;

    /**
     * The date that the trip should depart (or arrive, for requests where arriveBy is true).
     *
     * @deprecated use newer `timestampMillis` parameter, which is timezone independent
     */
    @Deprecated
    @QueryParam("date")
    protected String date;

    /**
     * The time that the trip should depart (or arrive, for requests where arriveBy is true).
     *
     * @deprecated use newer `timestampMillis` parameter, which is timezone independent
     */
    @Deprecated
    @QueryParam("time")
    protected String time;

    /**
     * The time that the trip should depart (or arrive, for requests where arriveBy is true) in milliseconds.
     */
    @QueryParam("timestampMillis")
    protected Long timestampMillis;

    /**
     * Whether the trip should depart or arrive at the specified date and time.
     */
    @QueryParam("arriveBy")
    protected Boolean arriveBy;

    /**
     * Whether the trip must be wheelchair accessible.
     */
    @QueryParam("wheelchair")
    protected Boolean wheelchair;

    /**
     * The maximum distance (in meters) the user is willing to walk. Defaults to unlimited.
     */
    @QueryParam("maxWalkDistance")
    protected Double maxWalkDistance;

    /**
     * The type of walk limit. Defaults to true.
     */
    @QueryParam("softWalkLimit")
    private Boolean softWalkLimit;

    /**
     * The maximum time (in seconds) of pre-transit travel when using drive-to-transit (park and
     * ride or kiss and ride). Defaults to unlimited.
     */
    @QueryParam("maxPreTransitTime")
    protected Integer maxPreTransitTime;

    /**
     * A multiplier for how bad walking is, compared to being in transit for equal lengths of time.
     * Defaults to 2. Empirically, values between 10 and 20 seem to correspond well to the concept
     * of not wanting to walk too much without asking for totally ridiculous itineraries, but this
     * observation should in no way be taken as scientific or definitive. Your mileage may vary.
     */
    @QueryParam("walkReluctance")
    protected Double walkReluctance;

    @QueryParam("kickscooterReluctance")
    protected Double kickscooterReluctance;

    @QueryParam("carReluctance")
    protected Double carReluctance;

    @QueryParam("motorbikeReluctance")
    protected Double motorbikeReluctance;

    @QueryParam("bicycleReluctance")
    protected Double bicycleReluctance;

    @QueryParam("rentingReluctance")
    protected Double rentingReluctance;

    /**
     * How much worse is waiting for a transit vehicle than being on a transit vehicle, as a
     * multiplier. The default value treats wait and on-vehicle time as the same.
     * <p>
     * It may be tempting to set this higher than walkReluctance (as studies often find this kind of
     * preferences among riders) but the planner will take this literally and walk down a transit
     * line to avoid waiting at a stop. This used to be set less than 1 (0.95) which would make
     * waiting offboard preferable to waiting onboard in an interlined trip. That is also
     * undesirable.
     * <p>
     * If we only tried the shortest possible transfer at each stop to neighboring stop patterns,
     * this problem could disappear.
     */
    @QueryParam("waitReluctance")
    protected Double waitReluctance;

    /**
     * How much less bad is waiting at the beginning of the trip (replaces waitReluctance)
     */
    @QueryParam("waitAtBeginningFactor")
    protected Double waitAtBeginningFactor;

    /**
     * The user's walking speed in meters/second. Defaults to approximately 3 MPH.
     */
    @QueryParam("walkSpeed")
    protected Double walkSpeed;

    /**
     * The user's biking speed in meters/second. Defaults to approximately 11 MPH, or 9.5 for bikeshare.
     */
    @QueryParam("bikeSpeed")
    protected Double bikeSpeed;

    /**
     * The user's car speed in meters/second. Defaults to approximately 90 MPH.
     */
    @QueryParam("carSpeed")
    protected Double carSpeed;

    /**
     * The time it takes the user to fetch their bike and park it again in seconds.
     * Defaults to 0.
     */
    @QueryParam("bikeSwitchTime")
    protected Integer bikeSwitchTime;

    /**
     * The cost of the user fetching their bike and parking it again.
     * Defaults to 0.
     */
    @QueryParam("bikeSwitchCost")
    protected Integer bikeSwitchCost;

    /**
     * For bike triangle routing, how much safety matters (range 0-1).
     */
    @QueryParam("triangleSafetyFactor")
    protected Double triangleSafetyFactor;

    /**
     * For bike triangle routing, how much slope matters (range 0-1).
     */
    @QueryParam("triangleSlopeFactor")
    protected Double triangleSlopeFactor;

    /**
     * For bike triangle routing, how much time matters (range 0-1).
     */
    @QueryParam("triangleTimeFactor")
    protected Double triangleTimeFactor;

    @QueryParam("differRangeGroups")
    protected Boolean differRangeGroups;
    /**
     * The set of characteristics that the user wants to optimize for. @See OptimizeType
     */
    @QueryParam("optimize")
    protected OptimizeType optimize;

    /**
     * <p>The set of modes that a user is willing to use, with qualifiers stating whether vehicles should be parked, rented, etc.</p>
     * <p>The possible values of the comma-separated list are:</p>
     *
     * <ul>
     *  <li>WALK</li>
     *  <li>TRANSIT</li>
     *  <li>BICYCLE</li>
     *  <li>BICYCLE_RENT</li>
     *  <li>BICYCLE_PARK</li>
     *  <li>CAR</li>
     *  <li>CAR_PARK</li>
     *  <li>TRAM</li>
     *  <li>SUBWAY</li>
     *  <li>RAIL</li>
     *  <li>BUS</li>
     *  <li>CABLE_CAR</li>
     *  <li>FERRY</li>
     *  <li>GONDOLA</li>
     *  <li>FUNICULAR</li>
     *  <li>AIRPLANE</li>
     * </ul>
     *
     * <p>
     *   For a more complete discussion of this parameter see <a href="http://docs.opentripplanner.org/en/latest/Configuration/#routing-modes">Routing modes</a>.
     * </p>
     */
    @QueryParam("mode")
    protected QualifiedModeSet modes;

    /**
     * Possible options: WALK, CAR, BIKE. Defaults to null, meaning we use old logic of getting starting mode from `mode` parameter.
     */
    @QueryParam("startingMode")
    protected TraverseMode startingMode;

    /**
     * Feature flag which activates vehicle renting possibility. Defaults to false.
     */
    @QueryParam("rentingAllowed")
    protected Boolean rentingAllowed;

    /**
     * Allows filtering vehicles for renting by fuel types. By default we accept renting all vehicles.
     */
    @QueryParam("fuelTypesAllowed")
    protected Set<FuelType> fuelTypesAllowed;

    /**
     * Allows filtering vehicles for renting by Gearbox. By default we accept renting all vehicles.
     */
    @QueryParam("gearboxesAllowed")
    protected Set<Gearbox> gearboxesAllowed;

    /**
     * Allows filtering vehicles for renting by providers. By default we accept renting all vehicles.
     */
    @QueryParam("providersAllowed")
    protected Set<String> providersAllowed;

    /**
     * Allows filtering vehicles for renting by providers. By default we accept renting all vehicles.
     */
    @QueryParam("providersDisallowed")
    protected Set<String> providersDisallowed;

    /**
     * Allows filtering vehicles for renting by vehicle types. By default we accept renting all vehicles.
     */
    @QueryParam("vehicleTypesAllowed")
    protected Set<VehicleType> vehicleTypesAllowed;

    /**
     * The minimum time, in seconds, between successive trips on different vehicles.
     * This is designed to allow for imperfect schedule adherence.  This is a minimum;
     * transfers over longer distances might use a longer time.
     */
    @QueryParam("minTransferTime")
    protected Integer minTransferTime;

    /**
     * The maximum number of possible itineraries to return.
     */
    @QueryParam("numItineraries")
    protected Integer numItineraries;

    /**
     * The list of preferred routes. The format is agency_[routename][_routeid], so TriMet_100 (100 is route short name)
     * or Trimet__42 (two underscores, 42 is the route internal ID).
     */
    @QueryParam("preferredRoutes")
    protected String preferredRoutes;

    /**
     * Penalty added for using every route that is not preferred if user set any route as preferred, i.e. number of seconds that we are willing
     * to wait for preferred route.
     */
    @QueryParam("otherThanPreferredRoutesPenalty")
    protected Integer otherThanPreferredRoutesPenalty;

    /**
     * The comma-separated list of preferred agencies.
     */
    @QueryParam("preferredAgencies")
    protected String preferredAgencies;

    /**
     * The list of unpreferred routes. The format is agency_[routename][_routeid], so TriMet_100 (100 is route short name) or Trimet__42 (two
     * underscores, 42 is the route internal ID).
     */
    @QueryParam("unpreferredRoutes")
    protected String unpreferredRoutes;

    /**
     * The comma-separated list of unpreferred agencies.
     */
    @QueryParam("unpreferredAgencies")
    protected String unpreferredAgencies;

    /**
     * Whether intermediate stops -- those that the itinerary passes in a vehicle, but
     * does not board or alight at -- should be returned in the response.  For example,
     * on a Q train trip from Prospect Park to DeKalb Avenue, whether 7th Avenue and
     * Atlantic Avenue should be included.
     */
    @QueryParam("showIntermediateStops")
    protected Boolean showIntermediateStops;

    /**
     * Prevents unnecessary transfers by adding a cost for boarding a vehicle. This is the cost that
     * is used when boarding while walking.
     */
    @QueryParam("walkBoardCost")
    protected Integer walkBoardCost;

    /**
     * Prevents unnecessary transfers by adding a cost for boarding a vehicle. This is the cost that
     * is used when boarding while cycling. This is usually higher that walkBoardCost.
     */
    @QueryParam("bikeBoardCost")
    protected Integer bikeBoardCost;

    /**
     * The comma-separated list of banned routes. The format is agency_[routename][_routeid], so TriMet_100 (100 is route short name) or Trimet__42
     * (two underscores, 42 is the route internal ID).
     */
    @QueryParam("bannedRoutes")
    protected String bannedRoutes;

    /**
     * Functions the same as bannnedRoutes, except only the listed routes are allowed.
     */
    @QueryParam("allowedRoutes")
    protected String allowedRoutes;

    /**
     * The comma-separated list of banned agencies.
     */
    @QueryParam("bannedAgencies")
    protected String bannedAgencies;

    /**
     * Functions the same as banned agencies, except only the listed agencies are allowed.
     */
    @QueryParam("allowedAgencies")
    protected String allowedAgencies;

    /**
     * The comma-separated list of banned trips.  The format is agency_trip[:stop*], so:
     * TriMet_24601 or TriMet_24601:0:1:2:17:18:19
     */
    @QueryParam("bannedTrips")
    protected String bannedTrips;

    /**
     * A comma-separated list of banned stops. A stop is banned by ignoring its
     * pre-board and pre-alight edges. This means the stop will be reachable via the
     * street network. Also, it is still possible to travel through the stop. Just
     * boarding and alighting is prohibited.
     * The format is agencyId_stopId, so: TriMet_2107
     */
    @QueryParam("bannedStops")
    protected String bannedStops;

    /**
     * A comma-separated list of banned stops. A stop is banned by ignoring its
     * pre-board and pre-alight edges. This means the stop will be reachable via the
     * street network. It is not possible to travel through the stop.
     * For example, this parameter can be used when a train station is destroyed, such
     * that no trains can drive through the station anymore.
     * The format is agencyId_stopId, so: TriMet_2107
     */
    @QueryParam("bannedStopsHard")
    protected String bannedStopsHard;

    /**
     * An additional penalty added to boardings after the first.  The value is in OTP's
     * internal weight units, which are roughly equivalent to seconds.  Set this to a high
     * value to discourage transfers.  Of course, transfers that save significant
     * time or walking will still be taken.
     */
    @QueryParam("transferPenalty")
    protected Integer transferPenalty;

    /**
     * An additional penalty added to boardings after the first when the transfer is not
     * preferred. Preferred transfers also include timed transfers. The value is in OTP's
     * internal weight units, which are roughly equivalent to seconds. Set this to a high
     * value to discourage transfers that are not preferred. Of course, transfers that save
     * significant time or walking will still be taken.
     * When no preferred or timed transfer is defined, this value is ignored.
     */
    @QueryParam("nonpreferredTransferPenalty")
    protected Integer nonpreferredTransferPenalty;

    /**
     * The maximum number of transfers (that is, one plus the maximum number of boardings)
     * that a trip will be allowed.  Larger values will slow performance, but could give
     * better routes.  This is limited on the server side by the MAX_TRANSFERS value in
     * org.opentripplanner.api.ws.Planner.
     */
    @QueryParam("maxTransfers")
    protected Integer maxTransfers;


    @QueryParam("compareNumberOfTransfers")
    protected Boolean compareNumberOfTransfers;
    /**
     * If true, goal direction is turned off and a full path tree is built (specify only once)
     */
    @QueryParam("batch")
    protected Boolean batch;

    /**
     * A transit stop required to be the first stop in the search (AgencyId_StopId)
     */
    @QueryParam("startTransitStopId")
    protected String startTransitStopId;

    /**
     * A transit trip acting as a starting "state" for depart-onboard routing (AgencyId_TripId)
     */
    @QueryParam("startTransitTripId")
    protected String startTransitTripId;

    /**
     * When subtracting initial wait time, do not subtract more than this value, to prevent overly
     * optimistic trips. Reasoning is that it is reasonable to delay a trip start 15 minutes to
     * make a better trip, but that it is not reasonable to delay a trip start 15 hours; if that
     * is to be done, the time needs to be included in the trip time. This number depends on the
     * transit system; for transit systems where trips are planned around the vehicles, this number
     * can be much higher. For instance, it's perfectly reasonable to delay one's trip 12 hours if
     * one is taking a cross-country Amtrak train from Emeryville to Chicago. Has no effect in
     * stock OTP, only in Analyst.
     * <p>
     * A value of 0 means that initial wait time will not be subtracted out (will be clamped to 0).
     * A value of -1 (the default) means that clamping is disabled, so any amount of initial wait
     * time will be subtracted out.
     */
    @QueryParam("clampInitialWait")
    protected Long clampInitialWait;

    /**
     * If true, this trip will be reverse-optimized on the fly. Otherwise, reverse-optimization
     * will occur once a trip has been chosen (in Analyst, it will not be done at all).
     */
    @QueryParam("reverseOptimizeOnTheFly")
    protected Boolean reverseOptimizeOnTheFly;

    @QueryParam("boardSlack")
    private Integer boardSlack;

    @QueryParam("alightSlack")
    private Integer alightSlack;

    @QueryParam("locale")
    private String locale;

    /**
     * If true, realtime updates are ignored during this search.
     */
    @QueryParam("ignoreRealtimeUpdates")
    protected Boolean ignoreRealtimeUpdates;

    /**
     * If true, the remaining weight heuristic is disabled. Currently only implemented for the long
     * distance path service.
     */
    @QueryParam("disableRemainingWeightHeuristic")
    protected Boolean disableRemainingWeightHeuristic;

    @QueryParam("remainingWeightMultiplier")
    protected Double remainingWeightMultiplier;

    @QueryParam("kickscooterRangeGroups")
    protected ArrayList<Double> kickscooterRangeGroups;
    /*
     * Control the size of flag-stop buffer returned in API response. This parameter only applies
     * to GTFS-Flex routing, which must be explicitly turned on via the useFlexService parameter in
     * router-config.json.
     */
    @QueryParam("flexFlagStopBufferSize")
    protected Double flexFlagStopBufferSize;

    /**
     * Whether to use reservation-based services
     */
    @QueryParam("flexUseReservationServices")
    protected Boolean flexUseReservationServices = true;

    /**
     * Whether to use eligibility-based services
     */
    @QueryParam("flexUseEligibilityServices")
    protected Boolean flexUseEligibilityServices = true;

    /**
     * Whether to ignore DRT time limits.
     * <p>
     * According to the GTFS-flex spec, demand-response transit (DRT) service must be reserved
     * at least `drt_advance_book_min` minutes in advance. OTP not allow DRT service to be used
     * inside that time window, unless this parameter is set to true.
     */
    @QueryParam("flexIgnoreDrtAdvanceBookMin")
    protected Boolean flexIgnoreDrtAdvanceBookMin;

    @QueryParam("maxHours")
    private Double maxHours;

    @QueryParam("useRequestedDateTimeInMaxHours")
    private Boolean useRequestedDateTimeInMaxHours;

    @QueryParam("disableAlertFiltering")
    private Boolean disableAlertFiltering;

    /**
     * If true, the Graph's ellipsoidToGeoidDifference is applied to all elevations returned by this query.
     */
    @QueryParam("geoidElevation")
    private Boolean geoidElevation;

    /**
     * Set the method of sorting itineraries in the response. Right now, the only supported value is "duration";
     * otherwise it uses default sorting. More sorting methods may be added in the future.
     */
    @QueryParam("pathComparator")
    private String pathComparator;

    @QueryParam("optimizationProfile")
    private String optimizationProfileName;

    @QueryParam("originalCostWeight")
    private Double originalCostWeight;

    @QueryParam("priceCostWeight")
    private Double priceCostWeight;

    @QueryParam("walkPrice")
    private Double walkPrice;

    /**
     * If true, we will be forced to use transit in all of the requested itineraries. Defaults to `false`
     */
    @QueryParam("forceTransitTrips")
    private Boolean forceTransitTrips;

    @QueryParam("vehiclePresenceThreshold")
    private Float vehiclePresenceThreshold;

    /*
     * somewhat ugly bug fix: the graphService is only needed here for fetching per-graph time zones.
     * this should ideally be done when setting the routing context, but at present departure/
     * arrival time is stored in the request as an epoch time with the TZ already resolved, and other
     * code depends on this behavior. (AMB)
     * Alternatively, we could eliminate the separate RoutingRequest objects and just resolve
     * vertices and timezones here right away, but just ignore them in semantic equality checks.
     */
    @Context
    protected OTPServer otpServer;

    /**
     * Range/sanity check the query parameter fields and build a Request object from them.
     *
     * @throws ParameterException when there is a problem interpreting a query parameter
     */
    protected RoutingRequest buildRequest() throws ParameterException {
        Router router = otpServer.getRouter(routerId);
        RoutingRequest request = router.defaultRoutingRequest.clone();

        request.vehicleValidator = new VehicleValidator();
        request.routingReluctances = new RoutingReluctances();
        request.routingDelays = new RoutingDelays();
        request.routerId = routerId;
        // The routing request should already contain defaults, which are set when it is initialized or in the JSON
        // router configuration and cloned. We check whether each parameter was supplied before overwriting the default.
        if (fromPlace != null)
            request.setFromString(fromPlace);

        if (toPlace != null)
            request.setToString(toPlace);

        setDateTime(router, request);

        if (wheelchair != null)
            request.setWheelchairAccessible(wheelchair);

        if (numItineraries != null)
            request.setNumItineraries(numItineraries);

        if (maxWalkDistance != null) {
            request.setMaxWalkDistance(maxWalkDistance);
            request.maxTransferWalkDistance = maxWalkDistance;
        }

        if (!Objects.isNull(softWalkLimit)) {
            request.setSoftWalkLimit(softWalkLimit);
        }

        if (maxPreTransitTime != null)
            request.setMaxPreTransitTime(maxPreTransitTime);

        if (walkReluctance != null)
            request.routingReluctances.setWalkReluctance(walkReluctance);

        if (waitReluctance != null)
            request.routingReluctances.setWaitReluctance(waitReluctance);

        if (kickscooterReluctance != null)
            request.routingReluctances.setKickScooterReluctance(kickscooterReluctance);

        if (carReluctance != null)
            request.routingReluctances.setCarReluctance(carReluctance);

        if (motorbikeReluctance != null)
            request.routingReluctances.setMotorbikeReluctance(motorbikeReluctance);

        if (bicycleReluctance != null)
            request.routingReluctances.setBicycleReluctance(bicycleReluctance);

        if (rentingReluctance != null)
            request.routingReluctances.setRentingReluctance(rentingReluctance);

        if (waitAtBeginningFactor != null)
            request.routingReluctances.setWaitAtBeginningFactor(waitAtBeginningFactor);

        if (walkSpeed != null && walkSpeed > 0)
            request.walkSpeed = walkSpeed;

        if (bikeSpeed != null && bikeSpeed > 0)
            request.bikeSpeed = bikeSpeed;

        if (carSpeed != null && carSpeed > 0)
            request.carSpeed = carSpeed;

        if (bikeSwitchTime != null)
            request.bike.setSwitchTime(bikeSwitchTime);

        if (bikeSwitchCost != null)
            request.bike.setSwitchCost(bikeSwitchCost);

        if (optimize != null) {
            // Optimize types are basically combined presets of routing parameters, except for triangle
            request.setOptimize(optimize);
            if (optimize == OptimizeType.TRIANGLE) {
                if (triangleSafetyFactor == null || triangleSlopeFactor == null || triangleTimeFactor == null) {
                    throw new ParameterException(Message.UNDERSPECIFIED_TRIANGLE);
                }
                if (triangleSafetyFactor == null && triangleSlopeFactor == null && triangleTimeFactor == null) {
                    throw new ParameterException(Message.TRIANGLE_VALUES_NOT_SET);
                }
                // FIXME couldn't this be simplified by only specifying TWO of the values?
                if (Math.abs(triangleSafetyFactor + triangleSlopeFactor + triangleTimeFactor - 1) > Math.ulp(1) * 3) {
                    throw new ParameterException(Message.TRIANGLE_NOT_AFFINE);
                }
                request.setTriangleSafetyFactor(triangleSafetyFactor);
                request.setTriangleSlopeFactor(triangleSlopeFactor);
                request.setTriangleTimeFactor(triangleTimeFactor);
            }
        }

        if (arriveBy != null)
            request.setArriveBy(arriveBy);

        if (showIntermediateStops != null)
            request.showIntermediateStops = showIntermediateStops;

        if (intermediatePlaces != null)
            request.setIntermediatePlacesFromStrings(intermediatePlaces);

        if (preferredRoutes != null)
            request.preferredTransit.setPreferredRoutes(preferredRoutes);

        if (otherThanPreferredRoutesPenalty != null)
            request.preferredTransit.setOtherThanPreferredRoutesPenalty(otherThanPreferredRoutesPenalty);

        if (preferredAgencies != null)
            request.preferredTransit.setPreferredAgencies(preferredAgencies);

        if (unpreferredRoutes != null)
            request.preferredTransit.setUnpreferredRoutes(unpreferredRoutes);

        if (unpreferredAgencies != null)
            request.preferredTransit.setUnpreferredAgencies(unpreferredAgencies);

        if (walkBoardCost != null)
            request.routingPenalties.setWalkBoardCost(walkBoardCost);

        if (bikeBoardCost != null)
            request.routingPenalties.setBikeBoardCost(bikeBoardCost);

        if (bannedRoutes != null)
            request.bannedTransit.setBannedRoutes(bannedRoutes);

        if (allowedRoutes != null)
            request.bannedTransit.setAllowedRoutes(allowedRoutes);

        if (bannedAgencies != null)
            request.bannedTransit.setBannedAgencies(bannedAgencies);

        if (allowedAgencies != null)
            request.bannedTransit.setAllowedAgencies(allowedAgencies);

        HashMap<FeedScopedId, BannedStopSet> bannedTripMap = makeBannedTripMap(bannedTrips);

        if (bannedTripMap != null)
            request.bannedTransit.setBannedTrips(bannedTripMap);

        if (bannedStops != null)
            request.bannedTransit.setBannedStops(bannedStops);

        if (bannedStopsHard != null)
            request.bannedTransit.setBannedStopsHard(bannedStopsHard);

        // The "Least transfers" optimization is accomplished via an increased transfer penalty.
        // See comment on RoutingRequest.transferPentalty.
        if (transferPenalty != null) request.routingPenalties.setTransferPenalty(transferPenalty);
        if (optimize == OptimizeType.TRANSFERS) {
            optimize = OptimizeType.QUICK;
            request.routingPenalties.setTransferPenalty(request.routingPenalties.getTransferPenalty() + 1800);
        }

        if (batch != null)
            request.batch = batch;

        if (optimize != null)
            request.setOptimize(optimize);

        /* Temporary code to get bike/car parking and renting working. */
        if (modes != null) {
            modes.applyToRoutingRequest(request);
            request.setModes(request.modes);
        }

        if (startingMode != null && startingMode.isOnStreetNonTransit()) {
            request.startingMode = startingMode;
        }

        if (rentingAllowed != null && rentingAllowed) {
            if (arriveBy != null && arriveBy) {
                throw new RuntimeException("Cannot combine rentingAllowed=true with arriveBy=true");
            }
            request.rentingAllowed = true;
            buildVehicleValidator(request);
        }

        if (request.bike.isAllowBikeRental() && bikeSpeed == null) {
            //slower bike speed for bike sharing, based on empirical evidence from DC.
            request.bikeSpeed = 4.3;
        }

        if (boardSlack != null)
            request.boardSlack = boardSlack;

        if (alightSlack != null)
            request.alightSlack = alightSlack;

        if (minTransferTime != null)
            request.transferSlack = minTransferTime; // TODO rename field in routingrequest

        if (nonpreferredTransferPenalty != null)
            request.preferredTransit.setNonpreferredTransferPenalty(nonpreferredTransferPenalty);

        if (request.boardSlack + request.alightSlack > request.transferSlack) {
            throw new RuntimeException("Invalid parameters: " +
                    "transfer slack must be greater than or equal to board slack plus alight slack");
        }

        if (maxTransfers != null)
            request.maxTransfers = maxTransfers;

        if (compareNumberOfTransfers != null)
            request.compareNumberOfTransfers = compareNumberOfTransfers;

        final long NOW_THRESHOLD_MILLIS = 15 * 60 * 60 * 1000;
        boolean tripPlannedForNow = Math.abs(request.getDateTime().getTime() - new Date().getTime()) < NOW_THRESHOLD_MILLIS;
        request.bike.setUseBikeRentalAvailabilityInformation(tripPlannedForNow); // TODO the same thing for GTFS-RT

        if (startTransitStopId != null && !startTransitStopId.isEmpty())
            request.startingTransitStopId = FeedScopedId.convertFromString(startTransitStopId);

        if (startTransitTripId != null && !startTransitTripId.isEmpty())
            request.startingTransitTripId = FeedScopedId.convertFromString(startTransitTripId);

        if (clampInitialWait != null)
            request.clampInitialWait = clampInitialWait;

        if (reverseOptimizeOnTheFly != null)
            request.reverseOptimizeOnTheFly = reverseOptimizeOnTheFly;

        if (ignoreRealtimeUpdates != null)
            request.ignoreRealtimeUpdates = ignoreRealtimeUpdates;

        if (disableRemainingWeightHeuristic != null)
            request.disableRemainingWeightHeuristic = disableRemainingWeightHeuristic;

        if (remainingWeightMultiplier != null)
            request.remainingWeighMultiplier = remainingWeightMultiplier;

        if (differRangeGroups != null)
            request.routingStateDiffOptions.differRangeGroups = differRangeGroups;

        if (kickscooterRangeGroups != null)
            request.routingStateDiffOptions.setKickscooterRangeGroupsInMeters(kickscooterRangeGroups);

        if (flexFlagStopBufferSize != null)
            request.flex.setFlagStopBufferSize(flexFlagStopBufferSize);

        if (flexUseReservationServices != null)
            request.flex.setUseReservationServices(flexUseReservationServices);

        if (flexUseEligibilityServices != null)
            request.flex.setUseEligibilityServices(flexUseEligibilityServices);

        if (flexIgnoreDrtAdvanceBookMin != null)
            request.flex.setIgnoreDrtAdvanceBookMin(flexIgnoreDrtAdvanceBookMin);

        if (maxHours != null)
            request.maxHours = maxHours;

        if (useRequestedDateTimeInMaxHours != null)
            request.useRequestedDateTimeInMaxHours = useRequestedDateTimeInMaxHours;

        if (disableAlertFiltering != null)
            request.disableAlertFiltering = disableAlertFiltering;

        if (geoidElevation != null)
            request.geoidElevation = geoidElevation;

        if (pathComparator != null)
            request.pathComparator = pathComparator;

        if (forceTransitTrips != null)
            request.forceTransitTrips = forceTransitTrips;

        if (vehiclePresenceThreshold != null)
            request.vehiclePredictionThreshold = vehiclePresenceThreshold;

        //getLocale function returns defaultLocale if locale is null
        request.locale = ResourceBundleSingleton.INSTANCE.getLocale(locale);

        Map<CostFunction.CostCategory, Double> costCategoryWeights = new HashMap<>();
        Optional.ofNullable(originalCostWeight).ifPresent(value -> costCategoryWeights.put(CostFunction.CostCategory.ORIGINAL, value));
        Optional.ofNullable(priceCostWeight).ifPresent(value -> costCategoryWeights.put(CostFunction.CostCategory.PRICE_ASSOCIATED, value));
        request.setCostCategoryWeights(costCategoryWeights);
        request.setOptimizationProfile(OptimizationProfileFactory.getOptimizationProfile(optimizationProfileName, request));

        if (Objects.nonNull(walkPrice))
            request.setWalkPrice(BigDecimal.valueOf(walkPrice));

        return request;
    }

    private void setDateTime(Router router, RoutingRequest request) {
        if (timestampMillis != null) {
            request.setDateTime(timestampMillis);
        } else {
            TimeZone tz;
            tz = router.graph.getTimeZone();
            if (date == null && time != null) { // Time was provided but not date
                LOG.debug("parsing ISO datetime {}", time);
                try {
                    // If the time query param doesn't specify a timezone, use the graph's default. See issue #1373.
                    DatatypeFactory df = javax.xml.datatype.DatatypeFactory.newInstance();
                    XMLGregorianCalendar xmlGregCal = df.newXMLGregorianCalendar(time);
                    GregorianCalendar gregCal = xmlGregCal.toGregorianCalendar();
                    if (xmlGregCal.getTimezone() == DatatypeConstants.FIELD_UNDEFINED) {
                        gregCal.setTimeZone(tz);
                    }
                    Date d2 = gregCal.getTime();
                    request.setDateTime(d2);
                } catch (DatatypeConfigurationException e) {
                    request.setDateTime(date, time, tz);
                }
            } else {
                request.setDateTime(date, time, tz);
            }
            request.flex.resetClockTime();
        }
    }

    private void buildVehicleValidator(RoutingRequest request) {
        if (!providersAllowed.isEmpty() && !providersDisallowed.isEmpty()) {
            throw new RuntimeException("Invalid parameters: " +
                    "cannot specify both providersAllowed and providersDisallowed");
        }

        if (!fuelTypesAllowed.isEmpty()) {
            request.vehicleValidator.addFilter(new FuelTypeFilter(fuelTypesAllowed));
        }

        if (!gearboxesAllowed.isEmpty()) {
            request.vehicleValidator.addFilter(new GearboxFilter(gearboxesAllowed));
        }

        if (!vehicleTypesAllowed.isEmpty()) {
            request.vehicleValidator.addFilter(new VehicleTypeFilter(vehicleTypesAllowed));
        }

        if (!providersAllowed.isEmpty()) {
            request.vehicleValidator.addFilter(ProviderFilter.providersAllowedFilter(providersAllowed));
        } else if (!providersDisallowed.isEmpty()) {
            request.vehicleValidator.addFilter(ProviderFilter.providersDisallowedFilter(providersDisallowed));
        }
    }

    /**
     * Take a string in the format agency:id or agency:id:1:2:3:4.
     * TODO Improve Javadoc. What does this even mean? Why are there so many colons and numbers?
     * Convert to a Map from trip --> set of int.
     */
    private HashMap<FeedScopedId, BannedStopSet> makeBannedTripMap(String banned) {
        if (banned == null) {
            return null;
        }

        HashMap<FeedScopedId, BannedStopSet> bannedTripMap = new HashMap<FeedScopedId, BannedStopSet>();
        String[] tripStrings = banned.split(",");
        for (String tripString : tripStrings) {
            // TODO this apparently allows banning stops within a trip with integers. Why?
            String[] parts = tripString.split(":");
            if (parts.length < 2) continue; // throw exception?
            String agencyIdString = parts[0];
            String tripIdString = parts[1];
            FeedScopedId tripId = new FeedScopedId(agencyIdString, tripIdString);
            BannedStopSet bannedStops;
            if (parts.length == 2) {
                bannedStops = BannedStopSet.ALL;
            } else {
                bannedStops = new BannedStopSet();
                for (int i = 2; i < parts.length; ++i) {
                    bannedStops.add(Integer.parseInt(parts[i]));
                }
            }
            bannedTripMap.put(tripId, bannedStops);
        }
        return bannedTripMap;
    }

}
