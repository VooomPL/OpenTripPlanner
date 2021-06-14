package org.opentripplanner.base;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.MultiPolygon;
import org.junit.Test;
import org.opentripplanner.IntegrationTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IsochroneIT extends IntegrationTest {

    @Test
    public void testWalkIsochrone() {
        javax.ws.rs.core.Response response = target("/routers/bydgoszcz/isochrone")
                .queryParam("fromPlace", "53.134802,17.991995")
                .queryParam("mode", "WALK")
                .queryParam("disableRemainingWeightHeuristic", "true")
                .queryParam("maxWalkDistance", "600")
                .queryParam("softWalkLimit", "false")
                .queryParam("cutoffSec", "300")
                .queryParam("cutoffSec", "600")
                .queryParam("cutoffSec", "900")
                .request()
                .header("Accept", "application/json")
                .get();

        FeatureCollection body = response.readEntity(FeatureCollection.class);

        assertThat(response.getStatus(), equalTo(200));
        assertIsochroneOk(body);
    }

    @Test
    public void testOwnCarIsochrone() {
        javax.ws.rs.core.Response response = target("/routers/bydgoszcz/isochrone")
                .queryParam("fromPlace", "53.12846,18.00187")
                .queryParam("mode", "WALK,CAR")
                .queryParam("startingMode", "CAR")
                .queryParam("disableRemainingWeightHeuristic", "true")
                .queryParam("maxWalkDistance", "20000")
                .queryParam("softWalkLimit", "false")
                .queryParam("cutoffSec", "300")
                .queryParam("cutoffSec", "600")
                .queryParam("cutoffSec", "900")
                .request()
                .header("Accept", "application/json")
                .get();

        FeatureCollection body = response.readEntity(FeatureCollection.class);

        assertThat(response.getStatus(), equalTo(200));
        assertIsochroneOk(body);
    }

    @Test
    public void testTransitIsochrone() {
        javax.ws.rs.core.Response response = target("/routers/bydgoszcz/isochrone")
                .queryParam("fromPlace", "53.134802,17.991995")
                .queryParam("mode", "WALK,TRANSIT")
                .queryParam("disableRemainingWeightHeuristic", "true")
                .queryParam("maxWalkDistance", "600")
                .queryParam("softWalkLimit", "false")
                .queryParam("time", "4:30pm")
                .queryParam("date", "07-22-2020")
                .queryParam("cutoffSec", "300")
                .queryParam("cutoffSec", "600")
                .queryParam("cutoffSec", "900")
                .request()
                .header("Accept", "application/json")
                .get();

        FeatureCollection body = response.readEntity(FeatureCollection.class);

        assertThat(response.getStatus(), equalTo(200));
        assertIsochroneOk(body);
    }

    @Test
    public void testRentableVehiclesIsochrone() {
        javax.ws.rs.core.Response response = target("/routers/bydgoszcz/isochrone")
                .queryParam("fromPlace", "53.134802,17.991995")
                .queryParam("mode", "WALK,CAR,BICYCLE")
                .queryParam("startingMode", "WALK")
                .queryParam("rentingAllowed", "true")
                .queryParam("disableRemainingWeightHeuristic", "true")
                .queryParam("maxWalkDistance", "600")
                .queryParam("softWalkLimit", "false")
                .queryParam("cutoffSec", "300")
                .queryParam("cutoffSec", "600")
                .queryParam("cutoffSec", "900")
                .request()
                .header("Accept", "application/json")
                .get();

        FeatureCollection body = response.readEntity(FeatureCollection.class);

        assertThat(response.getStatus(), equalTo(200));
        assertIsochroneOk(body);
    }

    @Test
    public void testRentableVehiclesAndTransitIsochrone() {
        javax.ws.rs.core.Response response = target("/routers/bydgoszcz/isochrone")
                .queryParam("fromPlace", "53.134802,17.991995")
                .queryParam("mode", "WALK,CAR,BICYCLE,TRANSIT")
                .queryParam("startingMode", "WALK")
                .queryParam("rentingAllowed", "true")
                .queryParam("disableRemainingWeightHeuristic", "true")
                .queryParam("maxWalkDistance", "600")
                .queryParam("softWalkLimit", "false")
                .queryParam("time", "4:30pm")
                .queryParam("date", "07-22-2020")
                .queryParam("cutoffSec", "300")
                .queryParam("cutoffSec", "600")
                .queryParam("cutoffSec", "900")
                .request()
                .header("Accept", "application/json")
                .get();

        FeatureCollection body = response.readEntity(FeatureCollection.class);

        assertThat(response.getStatus(), equalTo(200));
        assertIsochroneOk(body);
    }

    public void assertIsochroneOk(FeatureCollection body) {
        assertEquals(3, body.getFeatures().size());
        assertEquals((Integer) 900, body.getFeatures().get(0).getProperty("time"));
        assertEquals((Integer) 600, body.getFeatures().get(1).getProperty("time"));
        assertEquals((Integer) 300, body.getFeatures().get(2).getProperty("time"));
        for (Feature feature : body) {
            assertTrue(feature.getGeometry() instanceof MultiPolygon);
            assertTrue(((MultiPolygon) feature.getGeometry()).getCoordinates().size() > 0);
        }
    }
}
