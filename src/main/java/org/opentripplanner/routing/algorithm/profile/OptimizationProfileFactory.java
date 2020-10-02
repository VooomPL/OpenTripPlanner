package org.opentripplanner.routing.algorithm.profile;

import org.opentripplanner.routing.core.RoutingRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OptimizationProfileFactory {

    private static final Logger LOG = LoggerFactory.getLogger(OptimizationProfileFactory.class);

    private static final String PROFILE_NAME_ORIGINAL = "original";

    public OptimizationProfile getOptimizationProfile(String profileName, RoutingRequest request){
        OptimizationProfile profile = new OriginalOptimizationProfile(request);

        switch(profileName){
            case PROFILE_NAME_ORIGINAL:
                new OriginalOptimizationProfile(request);
                break;
            default:
                LOG.error("Optimization profile '"+profileName+"' undefined - returning default profile");
                break;
        }
        return profile;
    }

}
