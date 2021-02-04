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
    private final HashMap<Integer, TransitTicket> availableTickets = new HashMap<>();

    public BigDecimal computePrice(TransitTripDescription tripDescription) {
        if (tripDescription.isEmpty()) return BigDecimal.ZERO;

        BigDecimal[] memoizedCostsPerMinute = new BigDecimal[tripDescription.getLastMinute()];

        return getMinPrice(tripDescription.getLastMinute(), tripDescription, memoizedCostsPerMinute);
    }

    private BigDecimal getMinPrice(int minute, TransitTripDescription tripDescription, BigDecimal[] memoizedCostsPerMinute) {
        if (minute == 0) {
            return BigDecimal.ZERO;
        }
        if (Objects.nonNull(memoizedCostsPerMinute[minute - 1])) {
            LOG.info("Returning memoized value"); //TODO: does memoization even makes sense here?
            return memoizedCostsPerMinute[minute - 1];
        }
        if (tripDescription.isTravelingAtMinute(minute)) {
            List<BigDecimal> results = new ArrayList<>();
            for (TransitTicket ticketType : availableTickets.values()) {
                //TODO: allow discounts!!!
                results.add(getMinPrice(minute - ticketType.getTotalMinutesWhenValid(minute, tripDescription.getTripStages()),
                        tripDescription, memoizedCostsPerMinute).add(ticketType.getStandardPrice()));
            }

            Collections.sort(results);
            memoizedCostsPerMinute[minute - 1] = results.get(0);
            return results.get(0);
        } else {
            return BigDecimal.ZERO;
        }
    }

}
