package org.opentripplanner.pricing;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@AllArgsConstructor
public class TransitTicketType {

    @Getter
    private int id;

    @Getter
    private int maxMinutes;

    @Getter
    private BigDecimal standardPrice;


}
