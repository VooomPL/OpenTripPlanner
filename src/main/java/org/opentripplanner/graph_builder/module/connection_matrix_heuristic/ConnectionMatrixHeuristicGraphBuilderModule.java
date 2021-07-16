package org.opentripplanner.graph_builder.module.connection_matrix_heuristic;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.opentripplanner.graph_builder.services.GraphBuilderModule;
import org.opentripplanner.routing.algorithm.strategies.connection_matrix_heuristic.ConnectionMatrixHeuristicData;
import org.opentripplanner.routing.graph.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

@AllArgsConstructor
public class ConnectionMatrixHeuristicGraphBuilderModule implements GraphBuilderModule {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectionMatrixHeuristicGraphBuilderModule.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final File serializedStreetsFile;

    @Override
    public void buildGraph(Graph graph, HashMap<Class<?>, Object> extra) {
        try (InputStream fileStream = new FileInputStream(serializedStreetsFile)) {
            SerializedConnectionMatrixHeuristicData data = objectMapper
                    .readValue(fileStream, SerializedConnectionMatrixHeuristicData.class);
            graph.connectionMatrixHeuristicData = new ConnectionMatrixHeuristicData(data);
            LOG.info("Added connection matrix heuristic data to graph");
        } catch (IOException e) {
            LOG.error("Failed to build connection matrix heuristic from file {}", serializedStreetsFile, e);
        }
    }

    @Override
    public void checkInputs() {
    }
}
