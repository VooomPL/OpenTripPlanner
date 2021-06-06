package org.opentripplanner.base;

import org.junit.Test;
import org.opentripplanner.IntegrationTest;
import org.opentripplanner.api.model.Itinerary;
import org.opentripplanner.api.resource.Response;

import java.util.Random;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class LimitTransfersIT extends IntegrationTest {
    @Test
    public void testLimitingTransfers() {
        Random random = new Random(1);
        int remainingTries = 100;
        double maxLat = 53.2550, maxLon = 18.25346, minLat = 52.96911, minLon = 17.793526;
        double fromLat = 0, toLat = 0, fromLon = 0, toLon = 0;
        boolean foundProperRoute = false;
//Find request which uses 3 transfers
        while (remainingTries > 0 && !foundProperRoute) {
            remainingTries--;

            fromLat = minLat + random.nextDouble() * (maxLat - minLat);
            toLat = minLat + random.nextDouble() * (maxLat - minLat);
            fromLon = minLon + random.nextDouble() * (maxLon - minLon);
            toLon = minLon + random.nextDouble() * (maxLon - minLon);

            String fromPLace = fromLat + "," + fromLon;
            String toPLace = toLat + "," + toLon;

            javax.ws.rs.core.Response response = target("/routers/bydgoszcz/plan")
                    .queryParam("fromPlace", fromPLace)
                    .queryParam("toPlace", toPLace)
                    .queryParam("locale", "pl")
                    .queryParam("mode", "WALK,TRANSIT,CAR")
                    .queryParam("numItineraries", 1)
                    .queryParam("startingMode", "WALK")
                    .queryParam("softWalkLimit", "false")
                    .queryParam("date", "2020-10-23")
                    .queryParam("maxTransfers", "3")
                    .queryParam("rentingAllowed", "true")
                    .request().get();

            try {
                Response body = response.readEntity(Response.class);
                for (Itinerary itinerary : body.getPlan().itinerary) {
                    if (itinerary.transfers >= 3) {
                        foundProperRoute = true;
                    }
                }
            } catch (Exception e) {
            }
        }
        assertThat("Couldnt find route with transfers", foundProperRoute);

        String fromPLace = fromLat + "," + fromLon;
        String toPLace = toLat + "," + toLon;
//Decrease number of available transfers to 2
        javax.ws.rs.core.Response response = target("/routers/bydgoszcz/plan")
                .queryParam("fromPlace", fromPLace)
                .queryParam("toPlace", toPLace)
                .queryParam("locale", "pl")
                .queryParam("mode", "WALK,TRANSIT,CAR")
                .queryParam("startingMode", "WALK")
                .queryParam("numItineraries", 3)
                .queryParam("softWalkLimit", "false")
                .queryParam("date", "2020-10-23")
                .queryParam("compareNumberOfTransfers", "true")
                .queryParam("maxTransfers", "2") //Important difference!
                .queryParam("rentingAllowed", "true")
                .request().get();

        assertThat(response.getStatus(), equalTo(200));

        Response body = response.readEntity(Response.class);


        for (Itinerary itinerary : body.getPlan().itinerary) {
            assertThat("Found route with too many transfers", itinerary.transfers <= 2);
        }
    }

}
