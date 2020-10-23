package org.opentripplanner.routing.algorithm.profile;

import org.opentripplanner.routing.algorithm.costs.CostFunction;
import org.opentripplanner.routing.core.RoutingRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

public class OptimizationProfileFactory {

    private static final Logger LOG = LoggerFactory.getLogger(OptimizationProfileFactory.class);

    private static final String PROFILE_NAME_ORIGINAL = "original";
    private static final String PROFILE_NAME_PRICE_BASED = "cheapest";

    public static OptimizationProfile getOptimizationProfile(String profileName, RoutingRequest request, Map<CostFunction.CostCategory, Double> costWeights) {
        switch (Optional.ofNullable(profileName).orElse(PROFILE_NAME_ORIGINAL)) {
            case PROFILE_NAME_ORIGINAL:
                return new OriginalOptimizationProfile(request);
            case PROFILE_NAME_PRICE_BASED:
                return new PriceBasedOptimizationProfile(costWeights);
            default:
                LOG.error("Optimization profile '" + profileName + "' undefined - returning default profile");
                return getDefaultOptimizationProfile(request);
        }
    }

    public static OptimizationProfile getDefaultOptimizationProfile(RoutingRequest request) {
        return new OriginalOptimizationProfile(request);
    }
}
