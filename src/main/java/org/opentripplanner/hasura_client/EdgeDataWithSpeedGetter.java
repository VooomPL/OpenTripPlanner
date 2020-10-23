package org.opentripplanner.hasura_client;

import com.fasterxml.jackson.core.type.TypeReference;
import org.opentripplanner.hasura_client.mappers.HasuraToOTPMapper;
import org.slf4j.Logger;

public class EdgeDataWithSpeedGetter extends  HasuraGetter {
    @Override
    protected String query() {
        return "{\"query\": \"querytraffic {" +
                "  items:Traficdatawithspeed {\\n" +
                "   id\\n"+
                "    speed\\n" +
                "    startnodeid\\n" +
                "    endnodeid\\n" +
                "  }" +
                "}\"" +
                "}";


    }

    @Override
    protected Logger getLogger() {
        return null;
    }

    @Override
    protected HasuraToOTPMapper mapper() {
        return null;
    }

    @Override
    protected TypeReference<ApiResponse> hasuraType() {
        return null;
    }
}
