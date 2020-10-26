package org.opentripplanner.hasura_client;

import com.fasterxml.jackson.core.type.TypeReference;
import org.opentripplanner.hasura_client.mappers.EdgeDataWithSpeedMapper;
import org.opentripplanner.hasura_client.mappers.HasuraToOTPMapper;
import org.opentripplanner.updater.traficstreetupdater.EdgeDataWithSpeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EdgeDataWithSpeedGetter extends HasuraGetter {

    private static final Logger LOG = LoggerFactory.getLogger(HasuraGetter.class);

    @Override
    protected String query() {
        return "{\"query\": \"querytraffic {" +
                "  items:Traficdatawithspeed {\\n" +
                "   id\\n" +
                "    speed\\n" +
                "    startnodeid\\n" +
                "    endnodeid\\n" +
                "  }" +
                "}\"" +
                "}";


    }


    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    protected HasuraToOTPMapper<EdgeDataWithSpeed, EdgeDataWithSpeed> mapper() {
        return new EdgeDataWithSpeedMapper();
    }

    @Override
    protected TypeReference<ApiResponse<EdgeDataWithSpeed>> hasuraType() {
        return new TypeReference<ApiResponse<EdgeDataWithSpeed>>() {};

    }
}
