package org.opentripplanner.pricing.transit;

import org.junit.Test;
import org.opentripplanner.model.FeedScopedId;
import org.opentripplanner.model.Route;
import org.opentripplanner.model.Stop;
import org.opentripplanner.pricing.transit.ticket.TransitTicket;
import org.opentripplanner.pricing.transit.trip.model.TransitTripDescription;
import org.opentripplanner.pricing.transit.trip.model.TransitTripStage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TransitPriceCalculatorTest {

    private final TransitPriceCalculator priceCalculator = new TransitPriceCalculator();

    private final TransitTicket timeLimitedTicket20 = TransitTicket.builder(0, BigDecimal.valueOf(3.4)).setTimeLimit(20).build();
    private final TransitTicket timeLimitedTicket75 = TransitTicket.builder(1, BigDecimal.valueOf(4.4)).setTimeLimit(75).build();
    private final TransitTicket singleFareTicket = TransitTicket.builder(2, BigDecimal.valueOf(3.4)).setFaresNumberLimit(1).build();
    private final TransitTicket timeLimitedTicket90 = TransitTicket.builder(3, BigDecimal.valueOf(7)).setTimeLimit(90).build();
    private final TransitTicket timeLimitedTicketDaily = TransitTicket.builder(4, BigDecimal.valueOf(15)).setTimeLimit(1440).build();
    //TODO: add tickets with limitations (eg. zone-associated, stop/line-associated distance ticket types)

    //TODO: make sure that also null-associated scenarios are included

    @Test
    public void shouldReturn75minuteTicketPrice() {
        priceCalculator.getAvailableTickets().put(timeLimitedTicket20.getId(), timeLimitedTicket20);
        priceCalculator.getAvailableTickets().put(timeLimitedTicket75.getId(), timeLimitedTicket75);
        priceCalculator.getAvailableTickets().put(timeLimitedTicket90.getId(), timeLimitedTicket90);

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

        TransitTripDescription tripDescription = new TransitTripDescription(tripStages);

        BigDecimal transitPrice = priceCalculator.computePrice(tripDescription);
        assertEquals(0, transitPrice.compareTo(BigDecimal.valueOf(4.4)));
    }

    @Test
    public void shouldReturn3x20minuteTicketPrice() {
        priceCalculator.getAvailableTickets().put(timeLimitedTicket20.getId(), timeLimitedTicket20);
        priceCalculator.getAvailableTickets().put(timeLimitedTicketDaily.getId(), timeLimitedTicketDaily);

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

        TransitTripDescription tripDescription = new TransitTripDescription(tripStages);

        BigDecimal transitPrice = priceCalculator.computePrice(tripDescription);

        assertEquals(0, transitPrice.compareTo(BigDecimal.valueOf(10.2)));
    }

    @Test
    public void shouldReturn2xSingleFareTicketPrice() {
        priceCalculator.getAvailableTickets().put(timeLimitedTicket20.getId(), timeLimitedTicket20);
        priceCalculator.getAvailableTickets().put(singleFareTicket.getId(), singleFareTicket);
        priceCalculator.getAvailableTickets().put(timeLimitedTicketDaily.getId(), timeLimitedTicketDaily);

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

        TransitTripDescription tripDescription = new TransitTripDescription(tripStages);

        BigDecimal transitPrice = priceCalculator.computePrice(tripDescription);

        assertEquals(0, transitPrice.compareTo(BigDecimal.valueOf(6.8)));
    }
}
