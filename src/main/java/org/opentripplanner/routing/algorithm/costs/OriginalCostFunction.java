package org.opentripplanner.routing.algorithm.costs;

public class OriginalCostFunction implements CostFunction{

    @Override
    public double getCostWeight(CostCategory category, double cost) {
        return category.equals(CostCategory.ORIGINAL)?1:0;
    }

}
