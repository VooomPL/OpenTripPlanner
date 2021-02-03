package org.opentripplanner.pricing.transit;

import lombok.Getter;
import org.opentripplanner.pricing.transit.ticket.TransitTicket;
import org.opentripplanner.pricing.transit.trip.model.TransitTripStage;
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

    public BigDecimal computePrice(List<Integer> minutesWhenTraveling, List<TransitTripStage> tripStages) {
        if (minutesWhenTraveling.isEmpty() || tripStages.isEmpty()) return BigDecimal.ZERO;

        return computePrice(minutesWhenTraveling.get(minutesWhenTraveling.size() - 1), minutesWhenTraveling, tripStages);
    }

    private BigDecimal computePrice(int minutes, List<Integer> minutesWhenTraveling, List<TransitTripStage> tripStages) {
        if (minutes == 0) {
            return BigDecimal.ZERO;
        }
        //TODO: implement memoization-associated if here (and check later on if this is useful in any way)
        if (minutesWhenTraveling.contains(minutes)) {
            List<BigDecimal> results = new ArrayList<>();
            for (TransitTicket ticketType : availableTickets.values()) {
                //TODO: allow discounts!!!
                results.add(computePrice(minutes - ticketType.getTotalMinutesWhenValid(minutes, tripStages),
                        minutesWhenTraveling, tripStages).add(ticketType.getStandardPrice()));
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
