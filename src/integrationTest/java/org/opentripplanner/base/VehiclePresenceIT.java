package org.opentripplanner.base;

import org.junit.Test;
import org.opentripplanner.IntegrationTest;
import org.opentripplanner.api.resource.Response;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class VehiclePresenceIT extends IntegrationTest {

    @Test
    public void testResponseStructure() {
        javax.ws.rs.core.Response basicResponse = target("/routers/bydgoszcz/plan")
                .queryParam("fromPlace", "53.119934, 17.997763")
                .queryParam("toPlace", "53.142835, 18.018029")
                .queryParam("locale", "pl")
                .queryParam("mode", "WALK,CAR")
                .queryParam("startingMode", "WALK")
                .queryParam("rentingAllowed", "true")
                .queryParam("vehicleTypesAllowed", "CAR")
                .queryParam("time", "15:00:00")
                .queryParam("date", "07-22-2020")
                .request().get();

        javax.ws.rs.core.Response withPrediction = target("/routers/bydgoszcz/plan")
                .queryParam("fromPlace", "53.119934, 17.997763")
                .queryParam("toPlace", "53.142835, 18.018029")
                .queryParam("locale", "pl")
                .queryParam("mode", "WALK,CAR")
                .queryParam("startingMode", "WALK")
                .queryParam("rentingAllowed", "true")
                .queryParam("vehicleTypesAllowed", "CAR")
                .queryParam("time", "15:00:00")
                .queryParam("date", "07-22-2020")
                .queryParam("vehiclePresenceThreshold", "0.9")
                .request().get();

        Response basicBody = basicResponse.readEntity(Response.class);
        Response predictionBody = withPrediction.readEntity(Response.class);

        assertThat(basicResponse.getStatus(), equalTo(200));
        assertThat(withPrediction.getStatus(), equalTo(200));

        assertThat("a", equalTo("b"));
    }

}
