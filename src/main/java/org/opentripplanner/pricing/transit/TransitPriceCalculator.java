package org.opentripplanner.pricing.transit;

import lombok.Getter;
import org.opentripplanner.pricing.transit.ticket.TransitTicket;
import org.opentripplanner.pricing.transit.trip.model.TransitTripDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

public class TransitPriceCalculator {

    private static final Logger LOG = LoggerFactory.getLogger(TransitPriceCalculator.class);

    @Getter
    private final Set<TransitTicket> availableTickets = new HashSet<>();

    public BigDecimal computePrice(TransitTripDescription tripDescription) {
        if (tripDescription.isEmpty() || this.availableTickets.isEmpty()) return BigDecimal.ZERO;

        HashMap<Integer, BigDecimal> memoizedCostsPerMinute = new HashMap<>();

        BigDecimal transitTripPrice = getMinPrice(tripDescription.getLastMinute(), tripDescription, memoizedCostsPerMinute);
        BigDecimal returnedPrice = (transitTripPrice.compareTo(BigDecimal.ZERO) >= 0 ? transitTripPrice : BigDecimal.valueOf(-1));

        LOG.info("Computed {} transit trip price for trip {}", returnedPrice, tripDescription);

        return returnedPrice;
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
            LocalDateTime currentTimestamp = LocalDateTime.now();
            availableTickets.stream().filter(transitTicket -> transitTicket.isAvailable(currentTimestamp)).forEach(ticketType -> {
                int ticketValidForMinutes = ticketType.getTotalMinutesWhenValid(minute, tripDescription.getTripStages());
                if (ticketValidForMinutes != 0) {
                    results.add(getMinPrice(minute - ticketType.getTotalMinutesWhenValid(minute,
                            tripDescription.getTripStages()),
                            tripDescription, memoizedCostsPerMinute)
                            .add(ticketType.getStandardPrice()));
                }
            });
            if (!results.isEmpty()) {
                Collections.sort(results);
                memoizedCostsPerMinute.put(minute - 1, results.get(0));
                return results.get(0);
            } else {
                LOG.warn("No ticket valid for {}. minute of trip {} available", minute, tripDescription);
                return BigDecimal.valueOf(-Double.MAX_VALUE);
            }
        } else {
            //Walking from one transit trip to another - no need for ticket here
            return getMinPrice(tripDescription.getLastMinuteOfPreviousFare(minute), tripDescription, memoizedCostsPerMinute);
        }
    }

}
