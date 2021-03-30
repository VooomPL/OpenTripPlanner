package org.opentripplanner.pricing.transit;

import lombok.Value;

import java.math.BigDecimal;
import java.util.ArrayList;

@Value
public class TransitTripCost {

    BigDecimal price;
    ArrayList<String> ticketNames = new ArrayList<>();

}
