package org.opentripplanner.hasura_client;

import com.fasterxml.jackson.core.type.TypeReference;
import org.locationtech.jts.geom.Coordinate;
import org.opentripplanner.hasura_client.hasura_objects.HasuraObject;
import org.opentripplanner.hasura_client.mappers.HasuraToOTPMapper;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.vertextype.OsmVertex;
import org.opentripplanner.util.HttpUtils;
import org.slf4j.Logger;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

public abstract class HasuraGetter<GRAPH_OBJECT, HASURA_OBJECT extends HasuraObject> {
    protected abstract String query();

    protected abstract Logger getLogger();

    protected abstract HasuraToOTPMapper<HASURA_OBJECT, GRAPH_OBJECT> mapper();

    protected abstract TypeReference<ApiResponse<HASURA_OBJECT>> hasuraType();

    protected boolean addGeolocationArguments() {
        return true;
    }

    private String getGeolocationArguments(Graph graph) {
        List<OsmVertex> vertices = graph.getVertices().stream()
                .filter(v -> v instanceof OsmVertex)
                .map(v -> (OsmVertex) v)
                .collect(Collectors.toList());

        double latMin = vertices.stream().map(Vertex::getCoordinate).map(Coordinate::getY).min(Comparator.naturalOrder()).get();
        double lonMin = vertices.stream().map(Vertex::getCoordinate).map(Coordinate::getX).min(Comparator.naturalOrder()).get();
        double latMax = vertices.stream().map(Vertex::getCoordinate).map(Coordinate::getY).max(Comparator.naturalOrder()).get();
        double lonMax = vertices.stream().map(Vertex::getCoordinate).map(Coordinate::getX).max(Comparator.naturalOrder()).get();

        return "\"variables\": {" +
                "  \"latMin\": " + latMin + "," +
                "  \"lonMin\": " + lonMin + "," +
                "  \"latMax\": " + latMax + "," +
                "  \"lonMax\": " + lonMax +
                "}}";
    }


    public List<GRAPH_OBJECT> getFromHasura(Graph graph, String url) {
        String arguments = getGeolocationArguments(graph);
        String body = addGeolocationArguments() ? query() + arguments : query();
        ApiResponse<HASURA_OBJECT> response = HttpUtils.postData(url, body, hasuraType());
        getLogger().info("Got {} objects from API", response != null ? response.getData().getItems().size() : "null");
        return mapper().map(response != null ? response.getData().getItems() : emptyList());
    }
}
