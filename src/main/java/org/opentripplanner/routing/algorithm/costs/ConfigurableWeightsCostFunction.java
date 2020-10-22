package org.opentripplanner.routing.algorithm.costs;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class ConfigurableWeightsCostFunction implements CostFunction {

    private final Map<CostCategory, Double> costWeights;

    public ConfigurableWeightsCostFunction(Map<CostCategory, Double> costWeights) {
        this.costWeights = Optional.ofNullable(costWeights).orElse(Collections.emptyMap());
    }

    @Override
    public double getCostWeight(CostCategory category) {
        return Optional.ofNullable(costWeights.get(category)).orElse(0.0);
    }

}
