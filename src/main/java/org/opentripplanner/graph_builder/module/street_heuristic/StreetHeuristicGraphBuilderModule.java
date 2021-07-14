package org.opentripplanner.graph_builder.module.street_heuristic;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.opentripplanner.graph_builder.services.GraphBuilderModule;
import org.opentripplanner.routing.algorithm.strategies.street_heuristic.StreetHeuristicData;
import org.opentripplanner.routing.graph.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

@AllArgsConstructor
public class StreetHeuristicGraphBuilderModule implements GraphBuilderModule {

    private static final Logger LOG = LoggerFactory.getLogger(StreetHeuristicGraphBuilderModule.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final File serializedStreetsFile;

    @Override
    public void buildGraph(Graph graph, HashMap<Class<?>, Object> extra) {
        try (InputStream fileStream = new FileInputStream(serializedStreetsFile)) {
            SerializedStreetHeuristicData data = objectMapper
                    .readValue(fileStream, SerializedStreetHeuristicData.class);
            graph.streetHeuristicData = new StreetHeuristicData(data);
            LOG.info("Added street heuristic data to graph");
        } catch (IOException e) {
            LOG.error("Failed to build street heuristic from file {}", serializedStreetsFile);
        }
    }

    @Override
    public void checkInputs() {
    }
}
