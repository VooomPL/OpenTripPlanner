package org.opentripplanner.pricing.transit;

import lombok.NonNull;
import org.opentripplanner.pricing.transit.ticket.TransitTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class TransitPriceCalculator {

    private static final Logger LOG = LoggerFactory.getLogger(TransitPriceCalculator.class);

    private HashMap<Integer, TransitTicket> availableTicketTypes;
    private List<Integer> minutesWhenTraveling;

    public void setAvailableTicketTypes(List<TransitTicket> availableTicketTypesList) {
        this.availableTicketTypes = new HashMap<>();
        availableTicketTypesList.stream().forEach(ticketType -> this.availableTicketTypes.put(ticketType.getId(), ticketType));
    }

    //TODO: is there any better collection type for unavailableTicketTypesIMinute???
    public BigDecimal computePrice(@NonNull List<Integer> minutesWhenTraveling,
                                   @NonNull HashMap<Integer, List<Integer>> unavailableTicketTypesIMinute)
            throws NullPointerException {
        //TODO: make sure that there is no problem, when available tickets = null (or prevent this from happening)
        //TODO: empty unavailableTicketTypesIMinute means that all ticket types are avaialble at any part of the trip
        //TODO: include unavailableTicketTypes
        //TODO: co zrobić z jednorazowymi (lub kilko-przesiadkowymi - wziąć pod uwagę reguły przesiadek!!!!)
        this.minutesWhenTraveling = minutesWhenTraveling;

        return dp(minutesWhenTraveling.get(minutesWhenTraveling.size() - 1));
    }

    //TODO: change method name to sth more meaningful
    private BigDecimal dp(int minutes) {
        if (minutes == 0) {
            return BigDecimal.ZERO;
        }
        //TODO: implement memoization-associated if here (and check later on if this is useful in any way)
        if (minutesWhenTraveling.contains(minutes)) {
            List<BigDecimal> results = new ArrayList<>();
            //TODO: check, whether the ticket on the available tickets is available at this point and only if so, compute the price
            for (TransitTicket ticketType : availableTicketTypes.values()) {
                //TODO: allow discounts!!!
                results.add(dp(minutes - ticketType.getMaxMinutes()).add(ticketType.getStandardPrice()));
            }

            Collections.sort(results);
            //TODO: memoize the result before returning??? Does it give us any significant gain???
            //TODO: if one could detect, that the solution is worse than an already existing one and stop further computations, than it might be more efficient than memoization....
            return results.get(0);
        } else {
            return BigDecimal.ZERO;
        }
    }

}
