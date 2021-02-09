package org.opentripplanner.pricing.transit;

import lombok.Getter;
import org.opentripplanner.pricing.transit.ticket.TransitTicket;
import org.opentripplanner.pricing.transit.trip.model.TransitTripDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

public class TransitPriceCalculator {

    private static final Logger LOG = LoggerFactory.getLogger(TransitPriceCalculator.class);

    @Getter
    private final Set<TransitTicket> availableTickets = new HashSet<>();

    public BigDecimal computePrice(TransitTripDescription tripDescription) {
        if (tripDescription.isEmpty() || this.availableTickets.isEmpty()) return BigDecimal.ZERO;

        HashMap<Integer, BigDecimal> memoizedCostsPerMinute = new HashMap<>();

        return getMinPrice(tripDescription.getLastMinute(), tripDescription, memoizedCostsPerMinute);
    }

    private BigDecimal getMinPrice(int minute, TransitTripDescription tripDescription, HashMap<Integer, BigDecimal> memoizedCostsPerMinute) {
        if (minute == 0) {
            return BigDecimal.ZERO;
        }
        if (memoizedCostsPerMinute.get(minute - 1) != null) {
            return memoizedCostsPerMinute.get(minute - 1);
        }
        if (tripDescription.isTravelingAtMinute(minute)) {
            List<BigDecimal> results = new ArrayList<>();
            availableTickets.forEach(ticketType -> {
                //TODO: allow discounts!!!
                results.add(getMinPrice(minute - ticketType.getTotalMinutesWhenValid(minute,
                        tripDescription.getTripStages()),
                        tripDescription, memoizedCostsPerMinute)
                        .add(ticketType.getStandardPrice()));
            });

            Collections.sort(results);
            memoizedCostsPerMinute.put(minute - 1, results.get(0));
            return results.get(0);
        } else {
            //Walking from one transit trip to another
            return getMinPrice(tripDescription.getLastMinuteOfPreviousFare(minute), tripDescription, memoizedCostsPerMinute);
        }
    }

}
