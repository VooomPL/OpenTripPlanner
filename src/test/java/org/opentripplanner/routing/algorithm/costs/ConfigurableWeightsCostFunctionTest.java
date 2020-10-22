package org.opentripplanner.routing.algorithm.costs;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class ConfigurableWeightsCostFunctionTest {

    @Test
    public void shouldReturnGivenPriceAssociatedWeight() {
        //given
        Map<CostFunction.CostCategory, Double> costWeights = new HashMap<>();
        costWeights.put(CostFunction.CostCategory.ORIGINAL, 0.7);
        costWeights.put(CostFunction.CostCategory.PRICE_ASSOCIATED, 0.3);

        //when
        ConfigurableWeightsCostFunction costFunction = new ConfigurableWeightsCostFunction(costWeights);

        //then
        assertEquals(0.7, costFunction.getCostWeight(CostFunction.CostCategory.ORIGINAL), 0);
        assertEquals(0.3, costFunction.getCostWeight(CostFunction.CostCategory.PRICE_ASSOCIATED),0);
    }

    @Test
    public void shouldReturnDefaultWeightForPriceAssociatedCostOnly() {
        //given
        Map<CostFunction.CostCategory, Double> costWeights = new HashMap<>();
        costWeights.put(CostFunction.CostCategory.ORIGINAL, 0.7);

        //when
        ConfigurableWeightsCostFunction costFunction = new ConfigurableWeightsCostFunction(costWeights);

        //then
        assertEquals(0.7, costFunction.getCostWeight(CostFunction.CostCategory.ORIGINAL), 0);
        assertEquals(0.0, costFunction.getCostWeight(CostFunction.CostCategory.PRICE_ASSOCIATED),0);
    }

    @Test
    public void shouldReturnZeroWeightForAllTypesOfCosts() {
        //when
        ConfigurableWeightsCostFunction costFunction = new ConfigurableWeightsCostFunction(null);

        //then
        assertEquals(0.0, costFunction.getCostWeight(CostFunction.CostCategory.ORIGINAL), 0);
        assertEquals(0.0, costFunction.getCostWeight(CostFunction.CostCategory.PRICE_ASSOCIATED),0);
    }

}
