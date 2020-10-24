package org.opentripplanner.routing.algorithm.profile;

import org.opentripplanner.routing.core.RoutingRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

public class OptimizationProfileFactory {

    private static final Logger LOG = LoggerFactory.getLogger(OptimizationProfileFactory.class);

    private static final String PROFILE_NAME_ORIGINAL = "original";

    public static OptimizationProfile getOptimizationProfile(String profileName, RoutingRequest request) {
        switch (Optional.ofNullable(profileName).orElse(PROFILE_NAME_ORIGINAL)) {
            case PROFILE_NAME_ORIGINAL:
                return new OriginalOptimizationProfile(request);
            default:
                LOG.error("Optimization profile '" + profileName + "' undefined - returning default profile");
                return getDefaultOptimizationProfile(request);
        }
    }

    public static OptimizationProfile getDefaultOptimizationProfile(RoutingRequest request) {
        return new OriginalOptimizationProfile(request);
    }
}
