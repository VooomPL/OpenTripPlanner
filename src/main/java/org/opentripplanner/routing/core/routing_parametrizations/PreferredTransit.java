package org.opentripplanner.routing.core.routing_parametrizations;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.opentripplanner.model.Route;
import org.opentripplanner.routing.core.RouteMatcher;

import java.util.Collections;
import java.util.HashSet;

@Getter
@Setter
@EqualsAndHashCode
public class PreferredTransit implements Cloneable {

    /**
     * Penalty for using a non-preferred transfer
     */
    private int nonpreferredTransferPenalty = 180;

    /**
     * Penalty added for using every unpreferred route. We return number of seconds that we are willing to wait for preferred route.
     */
    public int useUnpreferredRoutesPenalty = 300;

    /**
     * Set of preferred routes by user.
     */
    public RouteMatcher preferredRoutes = RouteMatcher.emptyMatcher();

    /**
     * Set of preferred agencies by user.
     */
    public HashSet<String> preferredAgencies = new HashSet<>();

    /**
     * Penalty added for using every route that is not preferred if user set any route as preferred. We return number of seconds that we are willing
     * to wait for preferred route.
     */
    public int otherThanPreferredRoutesPenalty = 300;

    /**
     * Set of unpreferred routes for given user.
     */
    public RouteMatcher unpreferredRoutes = RouteMatcher.emptyMatcher();

    /**
     * Set of unpreferred agencies for given user.
     */
    public HashSet<String> unpreferredAgencies = new HashSet<>();

    public void setPreferredAgencies(String s) {
        if (!s.isEmpty()) {
            preferredAgencies = new HashSet<>();
            Collections.addAll(preferredAgencies, s.split(","));
        }
    }

    public void setPreferredRoutes(String s) {
        if (!s.isEmpty()) {
            preferredRoutes = RouteMatcher.parse(s);
        } else {
            preferredRoutes = RouteMatcher.emptyMatcher();
        }
    }

    public void setOtherThanPreferredRoutesPenalty(int penalty) {
        if (penalty < 0) penalty = 0;
        this.otherThanPreferredRoutesPenalty = penalty;
    }

    public void setUnpreferredAgencies(String s) {
        if (!s.isEmpty()) {
            unpreferredAgencies = new HashSet<>();
            Collections.addAll(unpreferredAgencies, s.split(","));
        }
    }

    public void setUnpreferredRoutes(String s) {
        if (!s.isEmpty()) {
            unpreferredRoutes = RouteMatcher.parse(s);
        } else {
            unpreferredRoutes = RouteMatcher.emptyMatcher();
        }
    }

    /**
     * Check if route is preferred according to this request.
     */
    public long preferencesPenaltyForRoute(Route route) {
        long preferences_penalty = 0;
        String agencyID = route.getAgency().getId();
        if ((preferredRoutes != null && !preferredRoutes.equals(RouteMatcher.emptyMatcher())) ||
                (preferredAgencies != null && !preferredAgencies.isEmpty())) {
            boolean isPreferedRoute = preferredRoutes != null && preferredRoutes.matches(route);
            boolean isPreferedAgency = preferredAgencies != null && preferredAgencies.contains(agencyID);
            if (!isPreferedRoute && !isPreferedAgency) {
                preferences_penalty += otherThanPreferredRoutesPenalty;
            } else {
                preferences_penalty = 0;
            }
        }
        boolean isUnpreferedRoute = unpreferredRoutes != null && unpreferredRoutes.matches(route);
        boolean isUnpreferedAgency = unpreferredAgencies != null && unpreferredAgencies.contains(agencyID);
        if (isUnpreferedRoute || isUnpreferedAgency) {
            preferences_penalty += useUnpreferredRoutesPenalty;
        }
        return preferences_penalty;
    }

    public PreferredTransit clone() {
        try {
            PreferredTransit clone = (PreferredTransit) super.clone();
            clone.preferredRoutes = preferredRoutes.clone();
            clone.preferredAgencies = (HashSet<String>) preferredAgencies.clone();
            clone.unpreferredRoutes = unpreferredRoutes.clone();
            clone.unpreferredAgencies = (HashSet<String>) unpreferredAgencies.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            /* this will never happen since our super is the cloneable object */
            throw new RuntimeException(e);
        }
    }
}
