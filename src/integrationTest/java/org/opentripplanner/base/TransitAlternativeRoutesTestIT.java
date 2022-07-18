package org.opentripplanner.base;

import org.junit.Test;
import org.opentripplanner.IntegrationTest;
import org.opentripplanner.api.model.Itinerary;
import org.opentripplanner.api.model.Leg;
import org.opentripplanner.api.resource.Response;

import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class TransitAlternativeRoutesTestIT extends IntegrationTest {

    @Test
    public void testAlternativeRoutes() {
        javax.ws.rs.core.Response response = target("/routers/bydgoszcz/plan")
                .queryParam("fromPlace", "53.12008703925447,17.997937357727583")
                .queryParam("toPlace", "53.11969893218923,18.00318528745414")
                .queryParam("locale", "pl")
                .queryParam("mode", "WALK,TRANSIT")
                .queryParam("startingMode", "WALK")
                .queryParam("softWalkLimit", "false")
                .queryParam("rentingAllowed", "false")
                .queryParam("timestampMillis", "1603444380000") // 2020-10-23 11:13:00
                .queryParam("numItineraries", "8")
                .request().get();

        Response body = response.readEntity(Response.class);

        assertThat(response.getStatus(), equalTo(200));
        assertThat(body.getPlan().itinerary.size(), equalTo(8));
        checkItinerary(body.getPlan().itinerary.get(0), "51", Set.of("56", "58"));
        checkItinerary(body.getPlan().itinerary.get(2), "60", Set.of("52", "55", "59"));
        checkItinerary(body.getPlan().itinerary.get(3), "56", Set.of("51", "58"));
        checkItinerary(body.getPlan().itinerary.get(4), "55", Set.of("52", "59", "60"));
        checkItinerary(body.getPlan().itinerary.get(5), "58", Set.of("51", "56"));
        checkItinerary(body.getPlan().itinerary.get(6), "52", Set.of("55", "59", "60"));
        checkItinerary(body.getPlan().itinerary.get(7), "59", Set.of("52", "55", "60"));
    }

    public void checkItinerary(Itinerary itinerary, String route, Set<String> alternativeRoutes) {
        assertEquals(3, itinerary.legs.size());
        Leg leg = itinerary.legs.get(1);
        assertEquals(route, leg.route);
        assertEquals(alternativeRoutes, leg.alternativeRoutes.stream().map(r -> r.route).collect(toSet()));
    }
}
