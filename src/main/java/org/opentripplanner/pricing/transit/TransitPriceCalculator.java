package org.opentripplanner.pricing.transit;

import lombok.Getter;
import org.opentripplanner.pricing.transit.ticket.TransitTicket;
import org.opentripplanner.pricing.transit.trip.model.TransitTripDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TransitPriceCalculator {

    private static final Logger LOG = LoggerFactory.getLogger(TransitPriceCalculator.class);

    @Getter
    private final Set<TransitTicket> availableTickets = new HashSet<>();

    public TransitTripCost computePrice(TransitTripDescription tripDescription) {
        if (tripDescription.isEmpty() || this.availableTickets.isEmpty()) return new TransitTripCost(BigDecimal.ZERO);

        HashMap<Integer, TransitTripCost> memoizedCostsPerMinute = new HashMap<>();

        TransitTripCost transitTripPrice = getMinPrice(tripDescription.getLastMinute(), tripDescription, memoizedCostsPerMinute);
        TransitTripCost returnedCost = (transitTripPrice.getPrice().compareTo(BigDecimal.ZERO) >= 0 ? transitTripPrice : new TransitTripCost(BigDecimal.valueOf(-1)));

        LOG.info("Computed {} transit trip price for trip {} with tickets {}", returnedCost.getPrice(), tripDescription, transitTripPrice.getTicketNames());

        return returnedCost;
    }

    /*
     * Compute the best price for arriving at minute of trip. It recursively calculates and memorises best prices for
     * times before this minute and uses them to figure out the best ticket combination.
     */
    private TransitTripCost getMinPrice(int tripMinute, TransitTripDescription tripDescription, HashMap<Integer, TransitTripCost> memoizedCostsPerMinute) {

        if (tripMinute == 0) {
            return new TransitTripCost(BigDecimal.ZERO);
        }
        if (memoizedCostsPerMinute.get(tripMinute - 1) != null) {
            return memoizedCostsPerMinute.get(tripMinute - 1);
        }
        if (tripDescription.isTravelingAtMinute(tripMinute)) {
            List<TransitTripCost> results = new ArrayList<>();
            LocalDateTime currentTimestamp = LocalDateTime.now();
            availableTickets.stream().filter(transitTicket -> transitTicket.isAvailable(currentTimestamp)).forEach(ticketType -> {
                int ticketValidForMinutes = ticketType.getTotalMinutesWhenValid(tripMinute, tripDescription.getTripStages());
                if (ticketValidForMinutes != 0) {
                    TransitTripCost earlierTripCost = getMinPrice(tripMinute - ticketValidForMinutes,
                            tripDescription, memoizedCostsPerMinute);
                    TransitTripCost totalTripCost = new TransitTripCost(earlierTripCost.getPrice()
                            .add(ticketType.getStandardPrice()));
                    totalTripCost.getTicketNames().addAll(earlierTripCost.getTicketNames());
                    totalTripCost.getTicketNames().add(ticketType.getName());
                    results.add(totalTripCost);
                }
            });
            if (!results.isEmpty()) {
                TransitTripCost lowestPrice = results.stream().min(new TransitTripCostComparator()).get();
                memoizedCostsPerMinute.put(tripMinute - 1, lowestPrice);
                return lowestPrice;
            } else {
                LOG.warn("No ticket valid for {}. minute of trip {} available", tripMinute, tripDescription);
                return new TransitTripCost(BigDecimal.valueOf(-Double.MAX_VALUE));
            }
        } else {
            //Walking from one transit trip to another - no need for ticket here
            return getMinPrice(tripDescription.getLastMinuteOfPreviousFare(tripMinute), tripDescription, memoizedCostsPerMinute);
        }
    }

}
