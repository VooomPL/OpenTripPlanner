package org.opentripplanner.base;

import org.junit.Test;
import org.opentripplanner.IntegrationTest;
import org.opentripplanner.api.resource.Response;
import org.opentripplanner.routing.core.TraverseMode;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class GtfsRealTimeIT extends IntegrationTest {

    @Test
    public void testGtfsRealTime() {
        javax.ws.rs.core.Response original = target("/routers/bydgoszcz/plan")
                .queryParam("fromPlace", "53.11480700000001,17.960333")
                .queryParam("toPlace", "53.1158916666667,18.0334805555556")
                .queryParam("locale", "pl")
                .queryParam("mode", "WALK,TRANSIT")
                .queryParam("startingMode", "WALK")
                .queryParam("rentingAllowed", "false")
                .queryParam("time", "4:30pm")
                .queryParam("date", "07-22-2020")
                .queryParam("numItineraries", "1")
                .queryParam("ignoreRealtimeUpdates", "true")
                .request().get();

        javax.ws.rs.core.Response realTime = target("/routers/bydgoszcz/plan")
                .queryParam("fromPlace", "53.11480700000001,17.960333")
                .queryParam("toPlace", "53.1158916666667,18.0334805555556")
                .queryParam("locale", "pl")
                .queryParam("mode", "WALK,TRANSIT")
                .queryParam("startingMode", "WALK")
                .queryParam("rentingAllowed", "false")
                .queryParam("time", "4:30pm")
                .queryParam("date", "07-22-2020")
                .queryParam("numItineraries", "1")
                .queryParam("ignoreRealtimeUpdates", "false")
                .request().get();

        Response bodyOriginal = original.readEntity(Response.class);
        Response bodyRealTime = realTime.readEntity(Response.class);

        assertThat(original.getStatus(), equalTo(200));

        assertThat(bodyOriginal.getPlan().itinerary.size(), equalTo(1));
        assertThat(bodyOriginal.getPlan().itinerary.get(0).legs.size(), equalTo(1));
        assertThat(bodyOriginal.getPlan().itinerary.get(0).legs.get(0).route, equalTo("69"));
        assertThat(bodyOriginal.getPlan().itinerary.get(0).legs.get(0).mode, equalTo(TraverseMode.BUS));
        assertThat(bodyOriginal.getPlan().itinerary.get(0).legs.get(0).endTime.toInstant(), equalTo(GregorianCalendar.from(ZonedDateTime.of(2020, 7, 22, 14, 53, 0, 0, ZoneOffset.UTC)).toInstant()));

        assertThat(bodyRealTime.getPlan().itinerary.size(), equalTo(1));
        assertThat(bodyRealTime.getPlan().itinerary.get(0).legs.size(), equalTo(1));
        assertThat(bodyRealTime.getPlan().itinerary.get(0).legs.get(0).route, not("69"));
        assertThat(bodyRealTime.getPlan().itinerary.get(0).legs.get(0).mode, equalTo(TraverseMode.BUS));
        assertThat(bodyRealTime.getPlan().itinerary.get(0).legs.get(0).endTime.toInstant(), not(GregorianCalendar.from(ZonedDateTime.of(2020, 7, 22, 14, 53, 0, 0, ZoneOffset.UTC)).toInstant()));

    }

}
