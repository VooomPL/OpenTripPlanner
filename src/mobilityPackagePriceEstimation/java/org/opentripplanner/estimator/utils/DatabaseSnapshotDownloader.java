package org.opentripplanner.estimator.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opentripplanner.estimator.hasura_client.ProvidersGetter;
import org.opentripplanner.estimator.hasura_client.VehicleStateSnapshotGetter;
import org.opentripplanner.hasura_client.hasura_objects.Vehicle;
import org.opentripplanner.routing.core.vehicle_sharing.Provider;
import org.opentripplanner.routing.graph.Graph;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class DatabaseSnapshotDownloader {

    private static final String DEFAULT_SNAPSHOT_FILE_NAME = "current_snapshot.json";
    private static final int DEFAULT_RETRY_NUMBER = 3;

    private final Graph graph;
    private final String databaseURL;
    private final String databasePassword;
    private final String snapshotDirectory;
    private final Map<Integer, Provider> vehicleProviders;

    public DatabaseSnapshotDownloader(Graph graph, String databaseURL, String databasePassword, String snapshotDirectory) {
        this.graph = graph;
        this.databaseURL = databaseURL;
        this.vehicleProviders = new HashMap<>();
        this.databasePassword = databasePassword;
        this.snapshotDirectory = snapshotDirectory;
    }

    public void initializeProviders() {
        ProvidersGetter providersGetter = new ProvidersGetter();
        this.vehicleProviders.clear();
        this.vehicleProviders.putAll(providersGetter.postFromHasura(this.graph, this.databaseURL).stream()
                .collect(Collectors.toMap(Provider::getProviderId, provider -> provider)));
    }

    public int downloadSnapshot(LocalDateTime timestamp) {
        int retries = 0;
        VehicleStateSnapshotGetter vehicleSnapshotGetter = new VehicleStateSnapshotGetter(this.vehicleProviders, timestamp);
        while (retries < DEFAULT_RETRY_NUMBER) {
            List<Vehicle> vehicles = vehicleSnapshotGetter.postFromHasuraWithPassword(this.graph, this.databaseURL, this.databasePassword);
            if (Objects.nonNull(vehicles)) {
                saveSnapshotData(vehicles);
                return vehicles.size();
            }
            try {
                Thread.sleep(2000);
                retries++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    private void saveSnapshotData(List<Vehicle> vehicles) {
        SnapshotData data = new SnapshotData();
        data.addAll(vehicles);

        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(new File(snapshotDirectory + DEFAULT_SNAPSHOT_FILE_NAME), data);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
