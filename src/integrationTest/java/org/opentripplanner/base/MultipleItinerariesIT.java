package org.opentripplanner.base;

import org.junit.Test;
import org.opentripplanner.IntegrationTest;
import org.opentripplanner.api.model.Itinerary;
import org.opentripplanner.api.resource.Response;
import org.opentripplanner.routing.core.TraverseMode;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class MultipleItinerariesIT extends IntegrationTest {

    @Test
    public void testForbiddingOnceUsedProviders() {
        javax.ws.rs.core.Response response = target("/routers/bydgoszcz/plan")
                .queryParam("fromPlace", "53.135721,17.913400")
                .queryParam("toPlace", "53.129740,17.990377")
                .queryParam("locale", "pl")
                .queryParam("mode", "WALK,CAR")
                .queryParam("vehicleTypesAllowed", "MOTORBIKE")
                .queryParam("startingMode", "WALK")
                .queryParam("softWalkLimit", "false")
                .queryParam("rentingAllowed", "true")
                .request().get();

        Response body = response.readEntity(Response.class);

        assertThat(response.getStatus(), equalTo(200));
        assertThat(body.getPlan().itinerary.size(), equalTo(3));

        for (Itinerary itinerary: body.getPlan().itinerary) {
            assertThat(itinerary.legs.size(), equalTo(2));
            assertThat(itinerary.itineraryType, equalTo("WALK+MOTORBIKE"));
            assertThat(itinerary.legs.get(0).mode, equalTo(TraverseMode.WALK));
            assertThat(itinerary.legs.get(1).mode, equalTo(TraverseMode.CAR));
        }
        assertItinerary0(body.getPlan().itinerary.get(0));
        assertItinerary1(body.getPlan().itinerary.get(1));
        assertItinerary2(body.getPlan().itinerary.get(2));
    }

    private void assertItinerary0(Itinerary itinerary) {
        assertThat(itinerary.legs.get(1).vehicleDescription.getProvider().getProviderName(), equalTo("VrumVrum"));
        assertThat(itinerary.legs.get(1).vehicleDescription.getProviderVehicleId(), equalTo("abcd"));
    }

    private void assertItinerary1(Itinerary itinerary) {
        assertThat(itinerary.legs.get(1).vehicleDescription.getProvider().getProviderName(), equalTo("Blinkee"));
        assertThat(itinerary.legs.get(1).vehicleDescription.getProviderVehicleId(), equalTo("9999"));
    }

    private void assertItinerary2(Itinerary itinerary) {
        assertThat(itinerary.legs.get(1).vehicleDescription.getProvider().getProviderName(), equalTo("PrOViDeRNamE"));
        assertThat(itinerary.legs.get(1).vehicleDescription.getProviderVehicleId(), equalTo("hmm"));
    }
}
