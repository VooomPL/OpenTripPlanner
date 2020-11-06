package org.opentripplanner.base;

import org.junit.Test;
import org.opentripplanner.IntegrationTest;
import org.opentripplanner.api.resource.Response;
import org.opentripplanner.routing.core.TraverseMode;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class VehiclePresenceIT extends IntegrationTest {

    @Test
    public void testVehicleThresholdParameter() {
        javax.ws.rs.core.Response basicResponse = target("/routers/bydgoszcz/plan")
                .queryParam("fromPlace", "53.119934, 17.997763")
                .queryParam("toPlace", "53.142835, 18.018029")
                .queryParam("locale", "pl")
                .queryParam("mode", "WALK,CAR")
                .queryParam("startingMode", "WALK")
                .queryParam("rentingAllowed", "true")
                .queryParam("vehicleTypesAllowed", "CAR")
                .queryParam("time", "03:00pm")
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
                .queryParam("time", "03:00pm")
                .queryParam("date", "07-22-2020")
                .queryParam("vehiclePresenceThreshold", "0.6")
                .request().get();

        Response basicBody = basicResponse.readEntity(Response.class);
        Response predictionBody = withPrediction.readEntity(Response.class);

        assertThat(basicResponse.getStatus(), equalTo(200));
        assertThat(withPrediction.getStatus(), equalTo(200));

        assertThat(basicBody.getPlan().itinerary.size(), equalTo(1));
        assertThat(basicBody.getPlan().itinerary.get(0).legs.size(), equalTo(2));
        assertThat(basicBody.getPlan().itinerary.get(0).legs.get(0).mode, equalTo(TraverseMode.WALK));
        assertThat(basicBody.getPlan().itinerary.get(0).legs.get(1).mode, equalTo(TraverseMode.CAR));

        assertThat(predictionBody.getPlan().itinerary.size(), equalTo(1));
        assertThat(predictionBody.getPlan().itinerary.get(0).legs.size(), equalTo(2));
        assertThat(predictionBody.getPlan().itinerary.get(0).legs.get(0).mode, equalTo(TraverseMode.WALK));
        assertThat(predictionBody.getPlan().itinerary.get(0).legs.get(1).mode, equalTo(TraverseMode.CAR));

        assertThat(basicBody.getPlan().itinerary.get(0).legs.get(1).vehicleDescription.getLatitude(),
                not(equalTo(predictionBody.getPlan().itinerary.get(0).legs.get(1).vehicleDescription.getLatitude())));
        assertThat(basicBody.getPlan().itinerary.get(0).legs.get(1).vehicleDescription.getLongitude(),
                not(equalTo(predictionBody.getPlan().itinerary.get(0).legs.get(1).vehicleDescription.getLongitude())));
    }

    @Test
    public void testDifferentThresholdValues() {

        javax.ws.rs.core.Response highThreshold = target("/routers/bydgoszcz/plan")
                .queryParam("fromPlace", "53.119934, 17.997763")
                .queryParam("toPlace", "53.142835, 18.018029")
                .queryParam("locale", "pl")
                .queryParam("mode", "WALK,CAR")
                .queryParam("startingMode", "WALK")
                .queryParam("rentingAllowed", "true")
                .queryParam("vehicleTypesAllowed", "CAR")
                .queryParam("time", "03:00pm")
                .queryParam("date", "07-22-2020")
                .queryParam("vehiclePresenceThreshold", "0.9")
                .request().get();

        javax.ws.rs.core.Response lowThreshold = target("/routers/bydgoszcz/plan")
                .queryParam("fromPlace", "53.119934, 17.997763")
                .queryParam("toPlace", "53.142835, 18.018029")
                .queryParam("locale", "pl")
                .queryParam("mode", "WALK,CAR")
                .queryParam("startingMode", "WALK")
                .queryParam("rentingAllowed", "true")
                .queryParam("vehicleTypesAllowed", "CAR")
                .queryParam("time", "03:00pm")
                .queryParam("date", "07-22-2020")
                .queryParam("vehiclePresenceThreshold", "0.6")
                .request().get();

        Response highBody = highThreshold.readEntity(Response.class);
        Response lowBody = lowThreshold.readEntity(Response.class);

        assertThat(highThreshold.getStatus(), equalTo(200));
        assertThat(highThreshold.getStatus(), equalTo(200));

        assertThat(lowBody.getPlan().itinerary.size(), equalTo(1));
        assertThat(lowBody.getPlan().itinerary.get(0).legs.size(), equalTo(2));
        assertThat(lowBody.getPlan().itinerary.get(0).legs.get(0).mode, equalTo(TraverseMode.WALK));
        assertThat(lowBody.getPlan().itinerary.get(0).legs.get(1).mode, equalTo(TraverseMode.CAR));

        assertThat(highBody.getPlan().itinerary.size(), equalTo(1));
        assertThat(highBody.getPlan().itinerary.get(0).legs.size(), equalTo(1));
        assertThat(highBody.getPlan().itinerary.get(0).legs.get(0).mode, equalTo(TraverseMode.WALK));
    }

}
