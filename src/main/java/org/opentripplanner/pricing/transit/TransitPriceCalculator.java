package org.opentripplanner.pricing.transit;

import lombok.Getter;
import org.opentripplanner.pricing.transit.ticket.TransitTicket;
import org.opentripplanner.pricing.transit.trip.model.TripDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class TransitPriceCalculator {

    private static final Logger LOG = LoggerFactory.getLogger(TransitPriceCalculator.class);

    @Getter
    private final HashMap<Integer, TransitTicket> availableTickets = new HashMap<>();

    public BigDecimal getMinPrice(TripDescription tripDescription) {
        if (tripDescription.isEmpty()) return BigDecimal.ZERO;

        return getMinPrice(tripDescription.getLastMinute(), tripDescription);
    }

    private BigDecimal getMinPrice(int minute, TripDescription tripDescription) {
        if (minute == 0) {
            return BigDecimal.ZERO;
        }
        //TODO: implement memoization-associated if here (and check later on if this is useful in any way)
        if (tripDescription.isTravelingAtMinute(minute)) {
            List<BigDecimal> results = new ArrayList<>();
            for (TransitTicket ticketType : availableTickets.values()) {
                //TODO: allow discounts!!!
                results.add(getMinPrice(minute - ticketType.getTotalMinutesWhenValid(minute, tripDescription.getTripStages()),
                        tripDescription).add(ticketType.getStandardPrice()));
            }

            Collections.sort(results);
            //TODO: memoize the result before returning??? Does it give us any significant gain???
            //TODO: if one could detect, that the solution is worse than an already existing one and stop further computations,
            // than it might be more efficient than memoization....
            return results.get(0);
        } else {
            return BigDecimal.ZERO;
        }
    }

}
