package org.opentripplanner.pricing.transit;

import org.junit.Test;
import org.opentripplanner.model.FeedScopedId;
import org.opentripplanner.model.Route;
import org.opentripplanner.model.Stop;
import org.opentripplanner.pricing.transit.ticket.TransitTicket;
import org.opentripplanner.pricing.transit.trip.model.TransitTripStage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class TransitPriceCalculatorTest {

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

    private TransitTicket timeLimitedTicket20 = TransitTicket.builder(0, BigDecimal.valueOf(3.4)).setTimeLimit(20).build();
    private TransitTicket timeLimitedTicket75 = TransitTicket.builder(1, BigDecimal.valueOf(4.4)).setTimeLimit(75).build();
    private TransitTicket singleFareTicket = TransitTicket.builder(2, BigDecimal.valueOf(4.4)).setFaresNumberLimit(1).build();
    private TransitTicket timeLimitedTicket90 = TransitTicket.builder(3, BigDecimal.valueOf(7)).setTimeLimit(90).build();
    private TransitTicket timeLimitedTicketDaily = TransitTicket.builder(4, BigDecimal.valueOf(15)).setTimeLimit(1440).build();
    //TODO: add tickets with limitations (eg. zone-associated, stop/line-associated distance ticket types)

    //TODO: make sure that also null-associated scenarios are included

    @Test
    public void shouldReturn75minuteTicketPrice() {
        List<TransitTicket> availableTicketTypes = new ArrayList<>();
        availableTicketTypes.add(timeLimitedTicket20);
        availableTicketTypes.add(timeLimitedTicket75);
        availableTicketTypes.add(timeLimitedTicket90);
        priceCalculator.setAvailableTickets(availableTicketTypes);

        List<Integer> minutesWhenTraveling = Arrays.asList(minutesWhenTravelling);

        Route firstRoute = new Route();
        firstRoute.setId(new FeedScopedId("ZTM", "105"));
        firstRoute.setShortName("105");
        Route secondRoute = new Route();
        secondRoute.setId(new FeedScopedId("ZTM", "13"));
        secondRoute.setShortName("13");

        Stop stop1 = new Stop();
        stop1.setZoneId("2");
        stop1.setId(new FeedScopedId());
        Stop stop8 = new Stop();
        stop8.setZoneId("2");
        stop8.setId(new FeedScopedId());
        Stop stop11 = new Stop();
        stop11.setZoneId("2");
        stop11.setId(new FeedScopedId());
        Stop stop16 = new Stop();
        stop16.setZoneId("2");
        stop16.setId(new FeedScopedId());
        Stop stop41 = new Stop();
        stop41.setZoneId("1/2");
        stop41.setId(new FeedScopedId());
        Stop stop47 = new Stop();
        stop47.setZoneId("1");
        stop47.setId(new FeedScopedId());
        Stop stop51 = new Stop();
        stop51.setZoneId("1");
        stop51.setId(new FeedScopedId());

        List<TransitTripStage> tripStages = new ArrayList<>();

        /*

        Routes used in the tested itinerary:

        |  10 min   |5 min|             35 min                  |     Travel time
        |<--------->|<--->|<----------------------------------->|
        0       7   10    15                        40     46   50    Arrive at stop time (minutes)
        |-------|---|-----|-------------------------|------|----|
        |           |     |                                     |
        |   105     |walk |               13                    |     Mean of transport

         */
        tripStages.add(new TransitTripStage(firstRoute, stop1, 1, 0));
        tripStages.add(new TransitTripStage(firstRoute, stop8, 8, 0));
        tripStages.add(new TransitTripStage(firstRoute, stop11, 11, 0));
        tripStages.add(new TransitTripStage(secondRoute, stop16, 16, 0));
        tripStages.add(new TransitTripStage(secondRoute, stop41, 41, 0));
        tripStages.add(new TransitTripStage(secondRoute, stop47, 47, 0));
        tripStages.add(new TransitTripStage(secondRoute, stop51, 51, 0));

        BigDecimal transitPrice = priceCalculator.computePrice(minutesWhenTraveling, tripStages);
        assertTrue(transitPrice.compareTo(BigDecimal.valueOf(4.4)) == 0);
    }

    @Test
    public void shouldReturn3x20minuteTicketPrice() {
        List<TransitTicket> availableTicketTypes = new ArrayList<>();
        availableTicketTypes.add(timeLimitedTicket20);
        availableTicketTypes.add(timeLimitedTicketDaily);
        priceCalculator.setAvailableTickets(availableTicketTypes);

        List<Integer> minutesWhenTraveling = Arrays.asList(minutesWhenTravelling);

        Route firstRoute = new Route();
        firstRoute.setId(new FeedScopedId("ZTM", "105"));
        firstRoute.setShortName("105");
        Route secondRoute = new Route();
        secondRoute.setId(new FeedScopedId("ZTM", "13"));
        secondRoute.setShortName("13");

        Stop stop1 = new Stop();
        stop1.setZoneId("2");
        stop1.setId(new FeedScopedId());
        Stop stop8 = new Stop();
        stop8.setZoneId("2");
        stop8.setId(new FeedScopedId());
        Stop stop11 = new Stop();
        stop11.setZoneId("2");
        stop11.setId(new FeedScopedId());
        Stop stop16 = new Stop();
        stop16.setZoneId("2");
        stop16.setId(new FeedScopedId());
        Stop stop41 = new Stop();
        stop41.setZoneId("1/2");
        stop41.setId(new FeedScopedId());
        Stop stop47 = new Stop();
        stop47.setZoneId("1");
        stop47.setId(new FeedScopedId());
        Stop stop51 = new Stop();
        stop51.setZoneId("1");
        stop51.setId(new FeedScopedId());

        List<TransitTripStage> tripStages = new ArrayList<>();

        /*

        Routes used in the tested itinerary:

        |  10 min   |5 min|             35 min                  |     Travel time
        |<--------->|<--->|<----------------------------------->|
        0       7   10    15                        40     46   50    Arrive at stop time (minutes)
        |-------|---|-----|-------------------------|------|----|
        |           |     |                                     |
        |   105     |walk |               13                    |     Mean of transport

         */
        tripStages.add(new TransitTripStage(firstRoute, stop1, 1, 0));
        tripStages.add(new TransitTripStage(firstRoute, stop8, 8, 0));
        tripStages.add(new TransitTripStage(firstRoute, stop11, 11, 0));
        tripStages.add(new TransitTripStage(secondRoute, stop16, 16, 0));
        tripStages.add(new TransitTripStage(secondRoute, stop41, 41, 0));
        tripStages.add(new TransitTripStage(secondRoute, stop47, 47, 0));
        tripStages.add(new TransitTripStage(secondRoute, stop51, 51, 0));

        BigDecimal transitPrice = priceCalculator.computePrice(minutesWhenTraveling, tripStages);

        assertTrue(transitPrice.compareTo(BigDecimal.valueOf(10.2)) == 0);
    }
}
