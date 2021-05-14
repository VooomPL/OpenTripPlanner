package org.opentripplanner.routing.core.routing_parametrizations;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.opentripplanner.model.FeedScopedId;
import org.opentripplanner.model.Route;
import org.opentripplanner.routing.core.RouteMatcher;
import org.opentripplanner.routing.core.StopMatcher;
import org.opentripplanner.routing.request.BannedStopSet;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

@Getter
@Setter
@EqualsAndHashCode
public class BannedTransit implements Cloneable {


    /**
     * Do not use certain named routes.
     * The paramter format is: feedId_routeId,feedId_routeId,feedId_routeId
     * This parameter format is completely nonstandard and should be revised for the 2.0 API, see issue #1671.
     */
    private RouteMatcher bannedRoutes = RouteMatcher.emptyMatcher();

    /**
     * Only use certain named routes
     */
    private RouteMatcher allowedRoutes = RouteMatcher.emptyMatcher();

    /**
     * Do not use certain named agencies
     */
    private HashSet<String> bannedAgencies = new HashSet<>();

    /**
     * Only use certain named agencies
     */
    private HashSet<String> allowedAgencies = new HashSet<>();

    /**
     * Do not use certain trips
     */
    private HashMap<FeedScopedId, BannedStopSet> bannedTrips = new HashMap<>();

    /**
     * Do not use certain stops. See for more information the bannedStops property in the RoutingResource class.
     */
    private StopMatcher bannedStops = StopMatcher.emptyMatcher();

    /**
     * Do not use certain stops. See for more information the bannedStopsHard property in the RoutingResource class.
     */
    private StopMatcher bannedStopsHard = StopMatcher.emptyMatcher();

    public void setBannedRoutes(String s) {
        if (!s.isEmpty()) {
            bannedRoutes = RouteMatcher.parse(s);
        } else {
            bannedRoutes = RouteMatcher.emptyMatcher();
        }
    }

    public void setAllowedRoutes(String s) {
        if (!s.isEmpty()) {
            allowedRoutes = RouteMatcher.parse(s);
        } else {
            allowedRoutes = RouteMatcher.emptyMatcher();
        }
    }

    public void setBannedStops(String s) {
        if (!s.isEmpty()) {
            bannedStops = StopMatcher.parse(s);
        } else {
            bannedStops = StopMatcher.emptyMatcher();
        }
    }

    public void setBannedStopsHard(String s) {
        if (!s.isEmpty()) {
            bannedStopsHard = StopMatcher.parse(s);
        } else {
            bannedStopsHard = StopMatcher.emptyMatcher();
        }
    }

    public void setBannedAgencies(String s) {
        if (!s.isEmpty()) {
            bannedAgencies = new HashSet<>();
            Collections.addAll(bannedAgencies, s.split(","));
        }
    }

    public void banTrip(FeedScopedId trip) {
        bannedTrips.put(trip, BannedStopSet.ALL);
    }

    public void setAllowedAgencies(String s) {
        if (!s.isEmpty()) {
            allowedAgencies = new HashSet<>();
            Collections.addAll(allowedAgencies, s.split(","));
        }
    }

    public boolean routeIsBanned(Route route) {
        /* check if agency is banned for this plan */
        if (bannedAgencies != null) {
            if (bannedAgencies.contains(route.getAgency().getId())) {
                return true;
            }
        }

        /* check if route banned for this plan */
        if (bannedRoutes != null) {
            if (bannedRoutes.matches(route)) {
                return true;
            }
        }

        boolean allowed = false;
        boolean allowanceInUse = false;

        /* check if agency is allowed for this plan */
        if (allowedAgencies != null && allowedAgencies.size() > 0) {
            allowanceInUse = true;
            if (allowedAgencies.contains(route.getAgency().getId())) {
                allowed = true;
            }
        }

        /* check if route is allowed for this plan */
        if (allowedRoutes != null && !allowedRoutes.isEmpty()) {
            allowanceInUse = true;
            if (allowedRoutes.matches(route)) {
                allowed = true;
            }
        }

        return allowanceInUse && !allowed;
    }

    public BannedTransit clone() {
        try {
            BannedTransit clone = (BannedTransit) super.clone();
            clone.bannedRoutes = bannedRoutes.clone();
            clone.allowedRoutes = allowedRoutes.clone();
            clone.bannedAgencies = (HashSet<String>) bannedAgencies.clone();
            clone.allowedAgencies = (HashSet<String>) allowedAgencies.clone();
            clone.bannedTrips = (HashMap<FeedScopedId, BannedStopSet>) bannedTrips.clone();
            clone.bannedStops = bannedStops.clone();
            clone.bannedStopsHard = bannedStopsHard.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            /* this will never happen since our super is the cloneable object */
            throw new RuntimeException(e);
        }
    }
}
