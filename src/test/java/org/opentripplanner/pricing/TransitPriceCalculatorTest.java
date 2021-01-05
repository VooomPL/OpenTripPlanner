package org.opentripplanner.pricing;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class TransitPriceCalculatorTest {

    private TransitTicketType timeLimitedTicket20 = new TransitTicketType(0, 20, BigDecimal.valueOf(3.4));
    private TransitTicketType timeLimitedTicket75 = new TransitTicketType(1, 75, BigDecimal.valueOf(4.4));
    private TransitTicketType timeLimitedTicketSingleFare = new TransitTicketType(2, -1, BigDecimal.valueOf(4.4));
    private TransitTicketType timeLimitedTicket90 = new TransitTicketType(3, 90, BigDecimal.valueOf(7));
    private TransitTicketType timeLimitedTicketDaily = new TransitTicketType(4, 1440, BigDecimal.valueOf(15));
    //TODO: add tickets with limitations (eg. zone-associated, stop/line-associated distance ticket types)

    /*
        This represents the following itinerary:
        1. walking for 4 minutes to transit stop
        2. travelling for the next 25 minutes by eg. bus (line 520)
        3. walking to the next stop and waiting for 5 minutes in total
        4. travelling for the next 13 minutes by eg. tram (line 15)
     */
    private Integer[] minutesWhenTravelling = {5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
            25, 26, 27, 28, 29, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46};

    private BigDecimal[] traveledDistance = {BigDecimal.valueOf(290), BigDecimal.valueOf(290), BigDecimal.valueOf(290),
            BigDecimal.valueOf(290), BigDecimal.valueOf(290), BigDecimal.valueOf(290), BigDecimal.valueOf(290),
            BigDecimal.valueOf(290), BigDecimal.valueOf(290), BigDecimal.valueOf(290), BigDecimal.valueOf(290),
            BigDecimal.valueOf(290), BigDecimal.valueOf(290), BigDecimal.valueOf(290), BigDecimal.valueOf(290),
            BigDecimal.valueOf(290), BigDecimal.valueOf(290), BigDecimal.valueOf(290), BigDecimal.valueOf(290),
            BigDecimal.valueOf(290), BigDecimal.valueOf(290), BigDecimal.valueOf(290), BigDecimal.valueOf(290),
            BigDecimal.valueOf(290), BigDecimal.valueOf(290), BigDecimal.valueOf(223.076923076923077),
            BigDecimal.valueOf(223.076923076923077), BigDecimal.valueOf(223.076923076923077),
            BigDecimal.valueOf(223.076923076923077), BigDecimal.valueOf(223.076923076923077),
            BigDecimal.valueOf(223.076923076923077), BigDecimal.valueOf(223.076923076923077),
            BigDecimal.valueOf(223.076923076923077), BigDecimal.valueOf(223.076923076923077),
            BigDecimal.valueOf(223.076923076923077), BigDecimal.valueOf(223.076923076923077),
            BigDecimal.valueOf(223.076923076923077), BigDecimal.valueOf(223.076923076923077)};

    private TransitPriceCalculator priceCalculator = new TransitPriceCalculator();

    //TODO: make sure that also null-associated scenarios are included

    @Test
    public void shouldReturn75minuteTicketPrice() {
        List<TransitTicketType> availableTicketTypes = new ArrayList<>();
        availableTicketTypes.add(timeLimitedTicket20);
        availableTicketTypes.add(timeLimitedTicket75);
        availableTicketTypes.add(timeLimitedTicket90);
        priceCalculator.setAvailableTicketTypes(availableTicketTypes);

        List<Integer> minutesWhenTraveling = Arrays.asList(minutesWhenTravelling);

        HashMap<Integer, List<Integer>> unavailableTicketTypesIMinute = new HashMap<>();

        BigDecimal transitPrice = priceCalculator.computePrice(minutesWhenTraveling, unavailableTicketTypesIMinute);
        assertTrue(transitPrice.compareTo(BigDecimal.valueOf(4.4)) == 0);
    }

    @Test
    public void shouldReturn3x20minuteTicketPrice() {
        List<TransitTicketType> availableTicketTypes = new ArrayList<>();
        availableTicketTypes.add(timeLimitedTicket20);
        availableTicketTypes.add(timeLimitedTicketDaily);
        priceCalculator.setAvailableTicketTypes(availableTicketTypes);

        List<Integer> minutesWhenTraveling = Arrays.asList(minutesWhenTravelling);

        HashMap<Integer, List<Integer>> unavailableTicketTypesInMinute = new HashMap<>();

        BigDecimal transitPrice = priceCalculator.computePrice(minutesWhenTraveling, unavailableTicketTypesInMinute);

        assertTrue(transitPrice.compareTo(BigDecimal.valueOf(10.2)) == 0);
    }

}
