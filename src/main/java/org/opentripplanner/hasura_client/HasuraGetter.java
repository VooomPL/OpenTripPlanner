package org.opentripplanner.hasura_client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.reflect.TypeToken;
import org.opentripplanner.hasura_client.hasura_objects.HasuraObject;
import org.opentripplanner.hasura_client.mappers.HasuraToOTPMapper;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;

public abstract class HasuraGetter<GRAPH_OBJECT, HASURA_OBJECT extends HasuraObject> {
    private static final Logger LOG = LoggerFactory.getLogger(HasuraGetter.class);

    protected abstract String QUERY();

    protected abstract HasuraToOTPMapper<HASURA_OBJECT, GRAPH_OBJECT> mapper();

    protected abstract TypeReference<ApiResponse<HASURA_OBJECT>> hasuraType();

    protected boolean addGeolocationArguments() {
        return true;
    }

    private String getGeolocationArguments(Graph graph) {
        double latMin = graph.getEnvelope().getLowerLeftLatitude();
        double lonMin = graph.getEnvelope().getLowerLeftLongitude();
        double latMax = graph.getEnvelope().getUpperRightLatitude();
        double lonMax = graph.getEnvelope().getUpperRightLongitude();
        return "\"variables\": {" +
                "  \"latMin\": " + latMin + "," +
                "  \"lonMin\": " + lonMin + "," +
                "  \"latMax\": " + latMax + "," +
                "  \"lonMax\": " + lonMax +
                "}}";
    }


    public List<GRAPH_OBJECT> getFromHasura(Graph graph, String url) {
        String arguments = getGeolocationArguments(graph);
        String body = addGeolocationArguments() ? QUERY() + arguments : QUERY();
        ApiResponse<HASURA_OBJECT> response = HttpUtils.postData(url, body, hasuraType());
        LOG.info("Got {} objects from API", response != null ? response.getData().getItems().size() : "null");
        return mapper().map(response != null ? response.getData().getItems() : emptyList());
    }
}
