package org.opentripplanner.pricing.transit;

import java.util.Comparator;

public class TransitTripCostComparator implements Comparator<TransitTripCost> {

    @Override
    public int compare(TransitTripCost o1, TransitTripCost o2) {
        return o1.getPrice().compareTo(o2.getPrice());
    }
}
