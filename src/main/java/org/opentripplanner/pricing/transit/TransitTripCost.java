package org.opentripplanner.pricing.transit;

import lombok.Value;

import java.math.BigDecimal;
import java.util.ArrayList;

@Value
public class TransitTripCost implements Comparable<TransitTripCost> {

    BigDecimal price;
    ArrayList<String> ticketNames = new ArrayList<>();

    @Override
    public int compareTo(TransitTripCost o) {
        return getPrice().compareTo(o.getPrice());
    }
}
