package org.opentripplanner.routing.algorithm.profile;

public class OptimizationProfileFactory {

    public OptimizationProfile getOptimizationProfile(String profileName){
        //TODO 1: take into account what query parameters in RoutingResource translate to which Optimization profiles
        //if(Objects.nonNull(optimizationProfileName))
        return new WeightBasedOptimizationProfile();
        //TODO 2: use OptimizationProfile in GraphPathFinder and AStar (and RoutingContext??)
    }

}
