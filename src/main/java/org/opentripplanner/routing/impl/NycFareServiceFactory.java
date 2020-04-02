package org.opentripplanner.routing.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.opentripplanner.model.OtpTransitService;
import org.opentripplanner.routing.services.FareService;
import org.opentripplanner.routing.services.FareServiceFactory;

public class NycFareServiceFactory implements FareServiceFactory {

    public FareService makeFareService() {
        return new NycFareServiceImpl();
    }

    @Override
    public void processGtfs(OtpTransitService transitService) {
    }

    @Override
    public void configure(JsonNode config) {
    }
}
