package org.opentripplanner.pricing.transit.trip.model;

import org.junit.Test;
import org.opentripplanner.model.FeedScopedId;
import org.opentripplanner.model.Route;
import org.opentripplanner.model.Stop;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TripDescriptionTest {

    @Test
    public void shouldCreateValidTripDescription() {
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

        TripDescription tripDescription = new TripDescription(tripStages);

        assertTrue(tripDescription.isTravelingAtMinute(1));
        assertTrue(tripDescription.isTravelingAtMinute(4));
        assertTrue(tripDescription.isTravelingAtMinute(11));
        assertTrue(tripDescription.isTravelingAtMinute(16));
        assertTrue(tripDescription.isTravelingAtMinute(40));
        assertTrue(tripDescription.isTravelingAtMinute(51));
        assertFalse(tripDescription.isTravelingAtMinute(12));
        assertFalse(tripDescription.isTravelingAtMinute(15));
        assertEquals(51, tripDescription.getLastMinute());
    }

    @Test
    public void shouldCreateEmptyTripDescription() {
        TripDescription tripDescription = new TripDescription(null);

        assertTrue(tripDescription.isEmpty());
    }

}
