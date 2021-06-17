package org.opentripplanner.routing.core;

import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;
import org.opentripplanner.api.parameter.QualifiedModeSet;
import org.opentripplanner.common.MavenVersion;
import org.opentripplanner.common.model.GenericLocation;
import org.opentripplanner.common.model.NamedPlace;
import org.opentripplanner.model.FeedScopedId;
import org.opentripplanner.routing.algorithm.costs.CostFunction;
import org.opentripplanner.routing.algorithm.profile.OptimizationProfile;
import org.opentripplanner.routing.core.routing_parametrizations.*;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleValidator;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.error.TrivialPathException;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.impl.DurationComparator;
import org.opentripplanner.routing.impl.PathComparator;
import org.opentripplanner.routing.spt.DominanceFunction;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.routing.spt.ShortestPathTree;
import org.opentripplanner.updater.vehicle_sharing.vehicles_positions.SharedVehiclesSnapshotLabel;
import org.opentripplanner.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

/**
 * A trip planning request. Some parameters may not be honored by the trip planner for some or all itineraries.
 * For example, maxWalkDistance may be relaxed if the alternative is to not provide a route.
 * <p>
 * All defaults should be specified here in the RoutingRequest, NOT as annotations on query parameters in web services
 * that create RoutingRequests. This establishes a priority chain for default values:
 * RoutingRequest field initializers, then JSON router config, then query parameters.
 */
public class RoutingRequest implements Cloneable, Serializable {

    private static final long serialVersionUID = MavenVersion.VERSION.getUID();

    private static final Logger LOG = LoggerFactory.getLogger(RoutingRequest.class);

    /**
     * The model that computes turn/traversal costs.
     * TODO: move this to the Router or the Graph if it doesn't clutter the code too much
     */
    public IntersectionTraversalCostModel traversalCostModel = new SimpleIntersectionTraversalCostModel();

    /* FIELDS UNIQUELY IDENTIFYING AN SPT REQUEST */

    /**
     * The complete list of incoming query parameters.
     */
    public final HashMap<String, String> parameters = new HashMap<String, String>();

    /**
     * The router ID -- internal ID to switch between router implementation (or graphs)
     */
    public String routerId = "";

    /**
     * The start location
     */
    public GenericLocation from;

    /**
     * The end location
     */
    public GenericLocation to;

    /**
     * An ordered list of intermediate locations to be visited.
     */
    public List<GenericLocation> intermediatePlaces;

    /**
     * The maximum distance (in meters) the user is willing to walk for access/egress legs.
     * Defaults to unlimited.
     */
    public double maxWalkDistance = Double.MAX_VALUE;

    /**
     * The maximum distance (in meters) the user is willing to walk for transfer legs.
     * Defaults to unlimited. Currently set to be the same value as maxWalkDistance.
     */
    public double maxTransferWalkDistance = Double.MAX_VALUE;

    /**
     * The maximum time (in seconds) of pre-transit travel when using drive-to-transit (park and
     * ride or kiss and ride). By default limited to 30 minutes driving, because if it's unlimited on
     * large graphs the search becomes very slow.
     */
    public int maxPreTransitTime = 30 * 60;

    /**
     * The worst possible time (latest for depart-by and earliest for arrive-by) to accept
     */
    public long worstTime = Long.MAX_VALUE;

    /**
     * The worst possible weight that we will accept when planning a trip.
     */
    public double maxWeight = Double.MAX_VALUE;

    /**
     * The maximum duration of a returned itinerary, in hours.
     */
    public double maxHours = Double.MAX_VALUE;

    /**
     * Whether maxHours limit should consider wait/idle time between the itinerary and the requested arrive/depart time.
     */
    public boolean useRequestedDateTimeInMaxHours = false;

    /**
     * The set of TraverseModes that a user is willing to use. Defaults to WALK | TRANSIT.
     */
    public TraverseModeSet modes = new TraverseModeSet("TRANSIT,WALK"); // defaults in constructor overwrite this

    public TraverseMode startingMode = null;

    public boolean rentingAllowed = false;

    public VehicleValidator vehicleValidator = new VehicleValidator();

    /**
     * Threshold for deciding when vehicle is assumed to be missing when using vehiclePresencePredictor functionality
     * Default 0 means it is always present.
     */
    public double vehiclePredictionThreshold = 0;

    /**
     * The set of characteristics that the user wants to optimize for -- defaults to QUICK, or optimize for transit time.
     */
    public OptimizeType optimize = OptimizeType.QUICK;
    // TODO this should be completely removed and done only with individual cost parameters
    // Also: apparently OptimizeType only affects BICYCLE mode traversal of street segments.
    // If this is the case it should be very well documented and carried over into the Enum name.

    /**
     * The epoch date/time that the trip should depart (or arrive, for requests where arriveBy is true)
     */
    public long dateTime = new Date().getTime() / 1000;

    /**
     * Whether the trip should depart at dateTime (false, the default), or arrive at dateTime.
     */
    public boolean arriveBy = false;

    /**
     * Whether the trip must be wheelchair accessible.
     */
    public boolean wheelchairAccessible = false;

    /**
     * The maximum number of itineraries to return.
     */
    private int numItineraries = 3;

    /**
     * The maximum slope of streets for wheelchair trips.
     */
    public double maxSlope = 0.0833333333333; // ADA max wheelchair ramp slope is a good default.

    /**
     * Whether the planner should return intermediate stops lists for transit legs.
     */
    public boolean showIntermediateStops = false;

    /**
     * max walk/bike speed along streets, in meters per second
     */
    public double walkSpeed;

    public double bikeSpeed;

    public double carSpeed;

    public Locale locale = new Locale("en", "US");

    /**
     * Used instead of walk reluctance for stairs
     */
    public double stairsReluctance = 2.0;

    /**
     * Multiplicative factor on expected turning time.
     */
    public double turnReluctance = 1.0;

    public BikeParameters bike;

    public RoutingDelays routingDelays;

    public RoutingReluctances routingReluctances;

    public RoutingPenalties routingPenalties;

    public RoutingStateDiffOptions routingStateDiffOptions = new RoutingStateDiffOptions();

    public BannedTransit bannedTransit;

    public PreferredTransit preferredTransit;

    /**
     * A global minimum transfer time (in seconds) that specifies the minimum amount of time that must pass between exiting one transit vehicle and
     * boarding another. This time is in addition to time it might take to walk between transit stops. This time should also be overridden by specific
     * transfer timing information in transfers.txt
     */
    // initialize to zero so this does not inadvertently affect tests, and let Planner handle defaults
    public int transferSlack = 0;

    /**
     * Invariant: boardSlack + alightSlack <= transferSlack.
     */
    public int boardSlack = 0;

    public int alightSlack = 0;
    /**
     * On default transfers are unlimited
     */
    public int maxTransfers = 100;

    public boolean compareNumberOfTransfers = false;

    /**
     * Extensions to the trip planner will require additional traversal options beyond the default
     * set. We provide an extension point for adding arbitrary parameters with an
     * extension-specific key.
     */
    public Map<Object, Object> extensions = new HashMap<Object, Object>();

    /**
     * Options specifically for the case that you are walking a bicycle.
     */
    public RoutingRequest bikeWalkingOptions;

    /**
     * This is true when a GraphPath is being traversed in reverse for optimization purposes.
     */
    public boolean reverseOptimizing = false;

    /**
     * when true, do not use goal direction or stop at the target, build a full SPT
     */
    public boolean batch = false;

    private OptimizationProfile optimizationProfile;

    private Map<CostFunction.CostCategory, Double> costCategoryWeights;

    private BigDecimal walkPrice = BigDecimal.valueOf(0.3);

    /**
     * The maximum wait time in seconds the user is willing to delay trip start. Only effective in Analyst.
     */
    public long clampInitialWait = -1;

    /**
     * When true, reverse optimize this search on the fly whenever needed, rather than reverse-optimizing the entire path when it's done.
     */
    public boolean reverseOptimizeOnTheFly = false;

    /**
     * When true, do a full reversed search to compact the legs of the GraphPath.
     */
    public boolean compactLegsByReversedSearch = false;

    public boolean reverseOptimizationEnabled = true;

    /**
     * When true, realtime updates are ignored during this search.
     */
    public boolean ignoreRealtimeUpdates = false;

    /**
     * If true, the remaining weight heuristic is disabled. Currently only implemented for the long
     * distance path service.
     */
    public boolean disableRemainingWeightHeuristic = false;

    public GtfsFlexParameters flex;

    /**
     * The routing context used to actually carry out this search. It is important to build States from TraverseOptions
     * rather than RoutingContexts,and just keep a reference to the context in the TraverseOptions, rather than using
     * RoutingContexts for everything because in some testing and graph building situations we need to build a bunch of
     * initial states with different times and vertices from a single TraverseOptions, without setting all the transit
     * context or building temporary vertices (with all the exception-throwing checks that entails).
     * <p>
     * While they are conceptually separate, TraverseOptions does maintain a reference to its accompanying
     * RoutingContext (and vice versa) so that both do not need to be passed/injected separately into tight inner loops
     * within routing algorithms. These references should be set to null when the request scope is torn down -- the
     * routing context becomes irrelevant at that point, since temporary graph elements have been removed and the graph
     * may have been reloaded.
     */
    public RoutingContext rctx;

    /**
     * A transit stop that this trip must start from
     */
    public FeedScopedId startingTransitStopId;

    /**
     * A trip where this trip must start from (depart-onboard routing)
     */
    public FeedScopedId startingTransitTripId;

    public boolean softWalkLimiting = true;
    public boolean softPreTransitLimiting = true;

    public double softWalkPenalty = 60.0; // a jump in cost when stepping over the walking limit
    public double softWalkOverageRate = 5.0; // a jump in cost for every meter over the walking limit

    public double preTransitPenalty = 300.0; // a jump in cost when stepping over the pre-transit time limit
    public double preTransitOverageRate = 10.0; // a jump in cost for every second over the pre-transit time limit

    public boolean parkAndRide = false;

    /**
     * The function that compares paths converging on the same vertex to decide which ones continue to be explored.
     */
    public DominanceFunction dominanceFunction = new DominanceFunction.Pareto();

    /**
     * Accept only paths that use transit (no street-only paths).
     */
    public boolean forceTransitTrips = false;

    /**
     * Option to disable the default filtering of GTFS-RT alerts by time.
     */
    public boolean disableAlertFiltering = false;

    /**
     * Whether to apply the ellipsoid->geoid offset to all elevations in the response
     */
    public boolean geoidElevation = false;

    /**
     * How many extra ServiceDays to look in the future (or back, if arriveBy=true)
     * <p>
     * This parameter allows the configuration of how far, in service days, OTP should look for
     * transit service when evaluating the next departure (or arrival) at a stop. In some cases,
     * for example for services which run weekly or monthly, it may make sense to increase this
     * value. Larger values will increase the search time. This does not affect a case where a
     * trip starts multiple service days in the past (e.g. a multiday ferry trip will not be
     * board-able after the 2nd day in the current implementation).
     */
    public int serviceDayLookout = 1;

    /**
     * Which path comparator to use
     */
    public String pathComparator = null;

    public Double remainingWeighMultiplier = 1.0;

    /**
     * Saves split edge which can be split on origin/destination search
     * <p>
     * This is used so that TrivialPathException is thrown if origin and destination search would split the same edge
     */
    private StreetEdge splitEdge = null;

    @Setter
    @Getter
    private SharedVehiclesSnapshotLabel acceptedSharedVehiclesSnapshotLabel = new SharedVehiclesSnapshotLabel();

    /* CONSTRUCTORS */

    /**
     * Constructor for options; modes defaults to walk and transit
     */
    public RoutingRequest() {
        routingDelays = new RoutingDelays();
        routingReluctances = new RoutingReluctances();
        routingPenalties = new RoutingPenalties();
        bike = new BikeParameters();
        flex = new GtfsFlexParameters();
        bannedTransit = new BannedTransit();
        preferredTransit = new PreferredTransit();
        // http://en.wikipedia.org/wiki/Walking
        walkSpeed = 1.33; // 1.33 m/s ~ 3mph, avg. human speed
        bikeSpeed = 5; // 5 m/s, ~11 mph, a random bicycling speed
        // http://en.wikipedia.org/wiki/Speed_limit
        carSpeed = 40; // 40 m/s, 144 km/h, above the maximum (finite) driving speed limit worldwide
        setModes(new TraverseModeSet(TraverseMode.WALK, TraverseMode.TRANSIT));
        bikeWalkingOptions = this;

        // So that they are never null.
        from = new GenericLocation();
        to = new GenericLocation();
    }

    public RoutingRequest(TraverseModeSet modes) {
        this();
        this.setModes(modes);
    }

    public RoutingRequest(QualifiedModeSet qmodes) {
        this();
        qmodes.applyToRoutingRequest(this);
    }

    public RoutingRequest(String qmodes) {
        this();
        new QualifiedModeSet(qmodes).applyToRoutingRequest(this);
    }

    public RoutingRequest(TraverseMode mode) {
        this();
        this.setModes(new TraverseModeSet(mode));
    }

    public RoutingRequest(TraverseMode mode, OptimizeType optimize) {
        this(new TraverseModeSet(mode), optimize);
    }

    public RoutingRequest(TraverseModeSet modeSet, OptimizeType optimize) {
        this();
        this.optimize = optimize;
        this.setModes(modeSet);
    }

    /* ACCESSOR/SETTER METHODS */

    public long getSecondsSinceEpoch() {
        return dateTime;
    }

    public void setArriveBy(boolean arriveBy) {
        this.arriveBy = arriveBy;
        bikeWalkingOptions.arriveBy = arriveBy;
        if (worstTime == Long.MAX_VALUE || worstTime == 0)
            worstTime = arriveBy ? 0 : Long.MAX_VALUE;
    }

    public void setMode(TraverseMode mode) {
        setModes(new TraverseModeSet(mode));
    }

    public void setModes(TraverseModeSet modes) {
        this.modes = modes;
        if (modes.getBicycle()) {
//            TODO decide if we want to keep this piece of logic. It might be totally redundant and obsolete.
            // This alternate routing request is used when we get off a bike to take a shortcut and are
            // walking alongside the bike. FIXME why are we only copying certain fields instead of cloning the request?
            bikeWalkingOptions = new RoutingRequest();
            bikeWalkingOptions.setArriveBy(this.arriveBy);
            bikeWalkingOptions.maxWalkDistance = maxWalkDistance;
            bikeWalkingOptions.maxPreTransitTime = maxPreTransitTime;
            bikeWalkingOptions.walkSpeed = walkSpeed * 0.8; // walking bikes is slow
//            bikeWalkingOptions.walkReluctance = walkReluctance * 2.7; // and painful
            bikeWalkingOptions.optimize = optimize;
            bikeWalkingOptions.modes = modes.clone();
            bikeWalkingOptions.modes.setBicycle(false);
            bikeWalkingOptions.modes.setWalk(true);
            bikeWalkingOptions.bike.setWalkingBike(true);
            bikeWalkingOptions.bike.setSwitchTime(bike.getSwitchTime());
            bikeWalkingOptions.bike.setSwitchCost(bike.getSwitchCost());
            bikeWalkingOptions.stairsReluctance = stairsReluctance * 5; // carrying bikes on stairs is awful
        } else if (modes.getCar()) {
            bikeWalkingOptions = new RoutingRequest();
            bikeWalkingOptions.setArriveBy(this.arriveBy);
            bikeWalkingOptions.maxWalkDistance = maxWalkDistance;
            bikeWalkingOptions.maxPreTransitTime = maxPreTransitTime;
            bikeWalkingOptions.modes = modes.clone();
            bikeWalkingOptions.modes.setBicycle(false);
            bikeWalkingOptions.modes.setWalk(true);
        }
    }

    public void setStartingMode(TraverseMode mode) {
        this.startingMode = mode;
    }

    public void setRentingAllowed(boolean rentingAllowed) {
        this.rentingAllowed = rentingAllowed;
    }

    public void setOptimize(OptimizeType optimize) {
        this.optimize = optimize;
        bikeWalkingOptions.optimize = optimize;
    }

    public void setWheelchairAccessible(boolean wheelchairAccessible) {
        this.wheelchairAccessible = wheelchairAccessible;
    }

    /**
     * only allow traversal by the specified mode; don't allow walking bikes. This is used during contraction to reduce the number of possible paths.
     */
    public void freezeTraverseMode() {
        bikeWalkingOptions = clone();
        bikeWalkingOptions.bikeWalkingOptions = new RoutingRequest(new TraverseModeSet());
    }

    /**
     * Add an extension parameter with the specified key. Extensions allow you to add arbitrary traversal options.
     */
    public void putExtension(Object key, Object value) {
        extensions.put(key, value);
    }

    /**
     * Determine if a particular extension parameter is present for the specified key.
     */
    public boolean containsExtension(Object key) {
        return extensions.containsKey(key);
    }

    /**
     * Get the extension parameter with the specified key.
     */
    @SuppressWarnings("unchecked")
    public <T> T getExtension(Object key) {
        return (T) extensions.get(key);
    }

    /**
     * Returns the model that computes the cost of intersection traversal.
     */
    public IntersectionTraversalCostModel getIntersectionTraversalCostModel() {
        return traversalCostModel;
    }

    /**
     * @return the maximum walk distance
     */
    public double getMaxWalkDistance() {
        return maxWalkDistance;
    }

    public boolean isSoftWalkLimitEnabled() {
        return softWalkLimiting;
    }

    public void setSoftWalkLimit(boolean softWalkLimitEnabled) {
        this.softWalkLimiting = softWalkLimitEnabled;
    }

    public void setFromString(String from) {
        this.from = GenericLocation.fromOldStyleString(from);
    }

    public void setToString(String to) {
        this.to = GenericLocation.fromOldStyleString(to);
    }

    /**
     * Clear the allowed modes.
     */
    public void clearModes() {
        modes.clear();
    }

    /**
     * Add a TraverseMode to the set of allowed modes.
     */
    public void addMode(TraverseMode mode) {
        modes.setMode(mode, true);
    }

    /**
     * Add multiple modes to the set of allowed modes.
     */
    public void addMode(List<TraverseMode> mList) {
        for (TraverseMode m : mList) {
            addMode(m);
        }
    }

    public Date getDateTime() {
        return new Date(dateTime * 1000);
    }

    public void setDateTime(long timestampMillis) {
        dateTime = timestampMillis / 1000;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime.getTime() / 1000;
    }

    public void setDateTime(String date, String time, TimeZone tz) {
        setDateTime(DateUtils.toDate(date, time, tz));
    }

    public int getNumItineraries() {
        if (modes.isTransit() || rentingAllowed) {
            return numItineraries;
        } else {
            // If transit and renting is not to be used, only search for one itinerary.
            return 1;
        }
    }

    public void setNumItineraries(int numItineraries) {
        this.numItineraries = numItineraries;
    }

    public String toString() {
        return toString(" ");
    }

    public String toString(String sep) {
        return from + sep + to + sep + getMaxWalkDistance() + sep + getDateTime() + sep
                + arriveBy + sep + optimize + sep + modes.getAsStr() + sep
                + getNumItineraries();
    }

    public void removeMode(TraverseMode mode) {
        modes.setMode(mode, false);
    }

    /**
     * Sets intermediatePlaces by parsing GenericLocations from a list of string.
     */
    public void setIntermediatePlacesFromStrings(List<String> intermediates) {
        this.intermediatePlaces = new ArrayList<GenericLocation>(intermediates.size());
        for (String place : intermediates) {
            intermediatePlaces.add(GenericLocation.fromOldStyleString(place));
        }
    }

    /**
     * Clears any intermediate places from this request.
     */
    public void clearIntermediatePlaces() {
        if (this.intermediatePlaces != null) {
            this.intermediatePlaces.clear();
        }
    }

    /**
     * Returns true if there are any intermediate places set.
     */
    public boolean hasIntermediatePlaces() {
        return this.intermediatePlaces != null && this.intermediatePlaces.size() > 0;
    }

    /**
     * Adds a GenericLocation to the end of the intermediatePlaces list. Will initialize intermediatePlaces if it is null.
     */
    public void addIntermediatePlace(GenericLocation location) {
        if (this.intermediatePlaces == null) {
            this.intermediatePlaces = new ArrayList<GenericLocation>();
        }
        this.intermediatePlaces.add(location);
    }

    public void setTriangleSafetyFactor(double triangleSafetyFactor) {
        bike.setTriangleSafetyFactor(triangleSafetyFactor);
        bikeWalkingOptions.bike.setTriangleSafetyFactor(triangleSafetyFactor);
    }

    public void setTriangleSlopeFactor(double triangleSlopeFactor) {
        bike.setTriangleSlopeFactor(triangleSlopeFactor);
        bikeWalkingOptions.bike.setTriangleSlopeFactor(triangleSlopeFactor);
    }

    public void setTriangleTimeFactor(double triangleTimeFactor) {
        bike.setTriangleTimeFactor(triangleTimeFactor);
        bikeWalkingOptions.bike.setTriangleTimeFactor(triangleTimeFactor);
    }

    public NamedPlace getFromPlace() {
        return this.from.getNamedPlace();
    }

    public NamedPlace getToPlace() {
        return this.to.getNamedPlace();
    }

    /* INSTANCE METHODS */

    @SuppressWarnings("unchecked")
    @Override
    public RoutingRequest clone() {
        try {
            RoutingRequest clone = (RoutingRequest) super.clone();
            clone.routingDelays = routingDelays.clone();
            clone.routingReluctances = routingReluctances.clone();
            clone.routingPenalties = routingPenalties.clone();
            clone.bike = bike.clone();
            clone.flex = flex.clone();
            clone.bannedTransit = bannedTransit.clone();
            clone.preferredTransit = preferredTransit.clone();
            if (this.bikeWalkingOptions != this)
                clone.bikeWalkingOptions = this.bikeWalkingOptions.clone();
            else
                clone.bikeWalkingOptions = clone;
            return clone;
        } catch (CloneNotSupportedException e) {
            /* this will never happen since our super is the cloneable object */
            throw new RuntimeException(e);
        }
    }

    public RoutingRequest reversedClone() {
        RoutingRequest ret = this.clone();
        ret.setArriveBy(!ret.arriveBy);
        ret.reverseOptimizing = !ret.reverseOptimizing; // this is not strictly correct
        ret.bike.setUseBikeRentalAvailabilityInformation(false);
        return ret;
    }

    // Set routing context with passed-in set of temporary vertices. Needed for intermediate places
    // as a consequence of the check that temporary vertices are request-specific.
    public void setRoutingContext(Graph graph, Collection<Vertex> temporaryVertices) {
        if (rctx == null) {
            // graphService.getGraph(routerId)
            this.rctx = new RoutingContext(this, graph, temporaryVertices);
            // check after back reference is established, to allow temp edge cleanup on exceptions
            this.rctx.check();
        } else {
            if (rctx.graph == graph) {
                LOG.debug("keeping existing routing context");
                return;
            } else {
                LOG.error("attempted to reset routing context using a different graph");
                return;
            }
        }
    }


    public void setRoutingContext(Graph graph) {
        setRoutingContext(graph, null);
    }

    /**
     * For use in tests. Force RoutingContext to specific vertices rather than making temp edges.
     */
    public void setRoutingContext(Graph graph, Edge fromBackEdge, Vertex from, Vertex to) {
        // normally you would want to tear down the routing context...
        // but this method is mostly used in tests, and teardown interferes with testHalfEdges
        // FIXME here, or in test, and/or in other places like TSP that use this method
        // if (rctx != null)
        // this.rctx.destroy();
        this.rctx = new RoutingContext(this, graph, from, to);
        this.rctx.originBackEdge = fromBackEdge;
    }

    public void setRoutingContext(Graph graph, Vertex from, Vertex to) {
        setRoutingContext(graph, null, from, to);
    }

    /**
     * For use in tests. Force RoutingContext to specific vertices rather than making temp edges.
     */
    public void setRoutingContext(Graph graph, String from, String to) {
        this.setRoutingContext(graph, graph.getVertex(from), graph.getVertex(to));
    }

    /**
     * Used in internals API. Make a RoutingContext with no origin or destination vertices specified.
     */
    public void setDummyRoutingContext(Graph graph) {
        this.setRoutingContext(graph, "", "");
    }

    public RoutingContext getRoutingContext() {
        return this.rctx;
    }

    /**
     * Equality does not mean that the fields of the two RoutingRequests are identical, but that they will produce the same SPT. This is particularly
     * important when the batch field is set to 'true'. Does not consider the RoutingContext, to allow SPT caching. Intermediate places are also not
     * included because the TSP solver will factor a single intermediate places routing request into several routing requests without intermediates
     * before searching.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RoutingRequest))
            return false;
        RoutingRequest other = (RoutingRequest) o;
        if (this.batch != other.batch)
            return false;
        boolean endpointsMatch;
        if (this.batch) {
            if (this.arriveBy) {
                endpointsMatch = to.equals(other.to);
            } else {
                endpointsMatch = from.equals(other.from);
            }
        } else {
            endpointsMatch = ((from == null && other.from == null) || from.equals(other.from))
                    && ((to == null && other.to == null) || to.equals(other.to));
        }
        return endpointsMatch
                && dateTime == other.dateTime
                && arriveBy == other.arriveBy
                && numItineraries == other.numItineraries // should only apply in non-batch?
                && walkSpeed == other.walkSpeed
                && bikeSpeed == other.bikeSpeed
                && carSpeed == other.carSpeed
                && maxWeight == other.maxWeight
                && worstTime == other.worstTime
                && maxTransfers == other.maxTransfers
                && modes.equals(other.modes)
                && startingMode == other.startingMode
                && rentingAllowed == other.rentingAllowed
                && vehicleValidator.equals(other.vehicleValidator)
                && wheelchairAccessible == other.wheelchairAccessible
                && optimize.equals(other.optimize)
                && maxWalkDistance == other.maxWalkDistance
                && maxTransferWalkDistance == other.maxTransferWalkDistance
                && maxPreTransitTime == other.maxPreTransitTime
                && maxSlope == other.maxSlope
                && routingReluctances.equals(other.routingReluctances)
                && routingDelays.equals(other.routingDelays)
                && routingPenalties.equals(other.routingPenalties)
                && bannedTransit.equals(other.bannedTransit)
                && transferSlack == other.transferSlack
                && boardSlack == other.boardSlack
                && alightSlack == other.alightSlack
                && preferredTransit.equals(other.preferredTransit)
                && bike.equals(other.bike)
                && stairsReluctance == other.stairsReluctance
                && extensions.equals(other.extensions)
                && clampInitialWait == other.clampInitialWait
                && reverseOptimizeOnTheFly == other.reverseOptimizeOnTheFly
                && ignoreRealtimeUpdates == other.ignoreRealtimeUpdates
                && disableRemainingWeightHeuristic == other.disableRemainingWeightHeuristic
                && Objects.equal(startingTransitTripId, other.startingTransitTripId)
                && disableAlertFiltering == other.disableAlertFiltering
                && geoidElevation == other.geoidElevation
                && flex.equals(other.flex)
                && serviceDayLookout == other.serviceDayLookout;
    }

    /**
     * Equality and hashCode should not consider the routing context, to allow SPT caching.
     * When adding fields to the hash code, pick a random large prime number that's not yet in use.
     */
    @Override
    public int hashCode() {
        int hashCode = new Double(walkSpeed).hashCode() + new Double(bikeSpeed).hashCode()
                + new Double(carSpeed).hashCode() + new Double(maxWeight).hashCode()
                + (int) (worstTime & 0xffffffff) + modes.hashCode()
                + startingMode.hashCode() + (rentingAllowed ? 3 : 0)
                + vehicleValidator.hashCode()
                + (arriveBy ? 8966786 : 0) + (wheelchairAccessible ? 731980 : 0)
                + optimize.hashCode() + new Double(maxWalkDistance).hashCode()
                + new Double(maxTransferWalkDistance).hashCode()
                + new Double(maxSlope).hashCode()
                + routingReluctances.hashCode()
                + routingDelays.hashCode() * 15485863
                + routingPenalties.hashCode()
                + bannedTransit.hashCode()
                + bike.hashCode()
                + new Double(stairsReluctance).hashCode() * 315595321
                + maxPreTransitTime * 63061489
                + new Long(clampInitialWait).hashCode() * 209477
                + new Boolean(reverseOptimizeOnTheFly).hashCode() * 95112799
                + new Boolean(ignoreRealtimeUpdates).hashCode() * 154329
                + new Boolean(disableRemainingWeightHeuristic).hashCode() * 193939
                + flex.hashCode()
                + new Boolean(disableRemainingWeightHeuristic).hashCode() * 193939
                + Integer.hashCode(serviceDayLookout) * 31558519;

        if (batch) {
            hashCode *= -1;
            // batch mode, only one of two endpoints matters
            if (arriveBy) {
                hashCode += to.hashCode() * 1327144003;
            } else {
                hashCode += from.hashCode() * 524287;
            }
            hashCode += numItineraries; // why is this only present here?
        } else {
            // non-batch, both endpoints matter
            hashCode += from.hashCode() * 524287;
            hashCode += to.hashCode() * 1327144003;
        }
        return hashCode;
    }

    /**
     * Tear down any routing context (remove temporary edges from edge lists)
     */
    public void cleanup() {
        if (this.rctx == null)
            LOG.warn("routing context was not set, cannot destroy it.");
        else {
            rctx.destroy();
            LOG.debug("routing context destroyed");
        }
    }

    /**
     * @param mode
     * @return The road speed for a specific traverse mode.
     */
    public double getSpeed(TraverseMode mode) {
        if (mode == null) {
            return Double.NaN;
        }
        switch (mode) {
            case WALK:
                return walkSpeed;
            case BICYCLE:
                return bikeSpeed;
            case CAR:
                return carSpeed;
            default:
                break;
        }
        throw new IllegalArgumentException("getSpeed(): Invalid mode " + mode);
    }

    /**
     * @return The highest speed for all possible road-modes.
     */
    public double getStreetSpeedUpperBound() {
        // Assume carSpeed > bikeSpeed > walkSpeed
        if (modes.getCar())
            return carSpeed;
        if (modes.getBicycle())
            return bikeSpeed;
        return walkSpeed;
    }

    /**
     * @return The time it actually takes to board a vehicle. Could be significant eg. on airplanes and ferries
     */
    public int getBoardTime(TraverseMode transitMode) {
        Integer i = this.rctx.graph.boardTimes.get(transitMode);
        return i == null ? 0 : i;
    }

    /**
     * @return The time it actually takes to alight a vehicle. Could be significant eg. on airplanes and ferries
     */
    public int getAlightTime(TraverseMode transitMode) {
        Integer i = this.rctx.graph.alightTimes.get(transitMode);
        return i == null ? 0 : i;
    }

    private String getRouteOrAgencyStr(HashSet<String> strings) {
        StringBuilder builder = new StringBuilder();
        for (String agency : strings) {
            builder.append(agency);
            builder.append(",");
        }
        if (builder.length() > 0) {
            // trim trailing comma
            builder.setLength(builder.length() - 1);
        }
        return builder.toString();
    }

    public void setMaxWalkDistance(double maxWalkDistance) {
        if (maxWalkDistance >= 0) {
            this.maxWalkDistance = maxWalkDistance;
            bikeWalkingOptions.maxWalkDistance = maxWalkDistance;
        }
    }

    public void setMaxPreTransitTime(int maxPreTransitTime) {
        if (maxPreTransitTime > 0) {
            this.maxPreTransitTime = maxPreTransitTime;
            bikeWalkingOptions.maxPreTransitTime = maxPreTransitTime;
        }
    }

    /**
     * Get the maximum expected speed over all transit modes.
     * TODO derive actual speeds from GTFS feeds. On the other hand, that's what the bidirectional heuristic does on the fly.
     */
    public double getTransitSpeedUpperBound() {
        if (modes.contains(TraverseMode.RAIL)) {
            return 84; // 300kph typical peak speed of a TGV
        }
        if (modes.contains(TraverseMode.CAR)) {
            return 40; // 130kph max speed of a car on a highway
        }
        // Considering that buses can travel on highways, return the same max speed for all other transit.
        return 40; // TODO find accurate max speeds
    }

    /**
     * Create a new ShortestPathTree instance using the DominanceFunction specified in this RoutingRequest.
     */
    public ShortestPathTree getNewShortestPathTree() {
        if (java.util.Objects.nonNull(this.optimizationProfile)) {
            return this.optimizationProfile.getDominanceFunction().getNewShortestPathTree(this);
        } else {
            // For backward compatibility with old components we use this.dominationFunction if optimizationProfile is
            // not set
            return this.dominanceFunction.getNewShortestPathTree(this);
        }
    }

    /**
     * Does nothing if different edge is split in origin/destination search
     * <p>
     * But throws TrivialPathException if same edge is split in origin/destination search.
     * <p>
     * used in {@link org.opentripplanner.graph_builder.linking.ToStreetEdgeLinker}}
     *
     * @param edge
     */
    public void canSplitEdge(StreetEdge edge) throws TrivialPathException {
        if (splitEdge == null) {
            splitEdge = edge;
        } else {
            if (splitEdge.equals(edge)) {
                throw new TrivialPathException();
            }
        }

    }

    public void setServiceDayLookout(int serviceDayLookout) {
        this.serviceDayLookout = serviceDayLookout;
    }

    public Comparator<GraphPath> getPathComparator(boolean compareStartTimes) {
        if ("duration".equals(pathComparator)) {
            return new DurationComparator();
        }
        return new PathComparator(compareStartTimes);
    }

    public OptimizationProfile getOptimizationProfile() {
        return optimizationProfile;
    }

    public void setOptimizationProfile(OptimizationProfile optimizationProfile) {
        this.optimizationProfile = optimizationProfile;
    }

    public Map<CostFunction.CostCategory, Double> getCostCategoryWeights() {
        return costCategoryWeights;
    }

    public void setCostCategoryWeights(Map<CostFunction.CostCategory, Double> costCategoryWeights) {
        this.costCategoryWeights = costCategoryWeights;
    }

    public void setWalkPrice(BigDecimal walkPrice) {
        this.walkPrice = walkPrice;
    }

    public BigDecimal getWalkPrice() {
        return walkPrice;
    }
}
