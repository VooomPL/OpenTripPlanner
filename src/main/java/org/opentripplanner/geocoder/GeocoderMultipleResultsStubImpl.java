package org.opentripplanner.geocoder;

import org.locationtech.jts.geom.Envelope;

import java.util.Collection;


public class GeocoderMultipleResultsStubImpl implements Geocoder {

    private Collection<GeocoderResult> results;

    public GeocoderMultipleResultsStubImpl(Collection<GeocoderResult> results) {
        this.results = results;
    }

    @Override
    public GeocoderResults geocode(String address, Envelope bbox) {
        return new GeocoderResults(results);
    }

}
