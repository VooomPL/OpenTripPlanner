package org.opentripplanner.geocoder;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GeocoderNullImplTest {

    @Test
    public void testGeocode() {
        Geocoder nullGeocoder = new GeocoderNullImpl();
        GeocoderResults result = nullGeocoder.geocode("121 elm street", null);
        assertEquals("stub response", GeocoderNullImpl.ERROR_MSG, result.getError());
    }
}