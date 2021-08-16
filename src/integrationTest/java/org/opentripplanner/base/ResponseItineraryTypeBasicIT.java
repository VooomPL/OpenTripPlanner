package org.opentripplanner.base;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.opentripplanner.IntegrationTest;
import org.opentripplanner.api.model.Itinerary;
import org.opentripplanner.api.resource.Response;

import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ResponseItineraryTypeBasicIT extends IntegrationTest {

    @Test
    public void testWalkAndBus() {
        javax.ws.rs.core.Response response = target("/routers/bydgoszcz/plan")
                .queryParam("fromPlace", "53.134802,17.991995")
                .queryParam("toPlace", "53.122338,18.0098715")
                .queryParam("locale", "pl")
                .queryParam("mode", "WALK,BUS")
                .queryParam("startingMode", "WALK")
                .queryParam("softWalkLimit", "false")
                .queryParam("rentingAllowed", "false")
                .queryParam("timestampMillis", "1603447200000") // 2020-10-23 12:00:00
                .request().get();

        Response body = response.readEntity(Response.class);

        assertThat(response.getStatus(), equalTo(200));
        assertThat(body.getPlan().itinerary.size(), equalTo(3));

        for (Itinerary it : body.getPlan().itinerary) {
            assertThat(it.itineraryType, equalTo("WALK+TRANSIT"));
        }
    }

    @Test
    public void testOnlyWalkMode() {
        javax.ws.rs.core.Response response = target("/routers/bydgoszcz/plan")
                .queryParam("fromPlace", "53.134802,17.991995")
                .queryParam("toPlace", "53.122338,18.0098715")
                .queryParam("locale", "pl")
                .queryParam("mode", "WALK")
                .queryParam("maxWalkDistance", "999999")
                .queryParam("startingMode", "WALK")
                .queryParam("softWalkLimit", "false")
                .queryParam("timestampMillis", "1594904400000") // 2020-07-16 15:00:00
                .request().get();
        Response body = response.readEntity(Response.class);

        assertThat(response.getStatus(), equalTo(200));
        assertThat(body.getPlan().itinerary.size(), equalTo(1));
        assertThat(body.getPlan().itinerary.get(0).itineraryType, equalTo("WALK"));
    }

    @Test
    public void testKickscooter() {
        javax.ws.rs.core.Response response = target("/routers/bydgoszcz/plan")
                .queryParam("fromPlace", "53.134802,17.991995")
                .queryParam("toPlace", "53.122338,18.0098715")
                .queryParam("locale", "pl")
                .queryParam("mode", "WALK,BICYCLE")
                .queryParam("vehicleTypesAllowed", "KICKSCOOTER")
                .queryParam("startingMode", "WALK")
                .queryParam("softWalkLimit", "false")
                .queryParam("rentingAllowed", "true")
                .queryParam("timestampMillis", "1594904400000") // 2020-07-16 15:00:00
                .request().get();
        Response body = response.readEntity(Response.class);

        assertThat(response.getStatus(), equalTo(200));
        assertThat(body.getPlan().itinerary.size(), equalTo(1));
        assertThat(body.getPlan().itinerary.get(0).itineraryType, equalTo("WALK+KICKSCOOTER"));
    }

    @Test
    public void testBusAndKickscooter() {
        javax.ws.rs.core.Response response = target("/routers/bydgoszcz/plan")
                .queryParam("fromPlace", "53.134802,17.991995")
                .queryParam("toPlace", "53.122338,18.0098715")
                .queryParam("locale", "pl")
                .queryParam("mode", "WALK,BICYCLE,BUS")
                .queryParam("startingMode", "WALK")
                .queryParam("softWalkLimit", "false")
                .queryParam("rentingAllowed", "true")
                .queryParam("vehicleTypesAllowed", "KICKSCOOTER")
                .queryParam("timestampMillis", "1594904400000") // 2020-07-16 15:00:00
                .request().get();
        Response body = response.readEntity(Response.class);

        assertThat(response.getStatus(), equalTo(200));
        assertThat(body.getPlan().itinerary.size(), equalTo(3));
        assertThat(body.getPlan().itinerary.stream().map(it -> it.itineraryType).collect(Collectors.toList()),
                Matchers.contains("WALK+KICKSCOOTER", "WALK+TRANSIT", "WALK+TRANSIT"));
    }
}
