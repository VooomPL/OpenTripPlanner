package org.opentripplanner.updater.vehicle_sharing.vehicles_positions;

import com.fasterxml.jackson.databind.JsonNode;
import org.opentripplanner.estimator.hasura_client.ProvidersGetter;
import org.opentripplanner.graph_builder.linking.TemporaryStreetSplitter;
import org.opentripplanner.hasura_client.VehicleHistoricalPositionsGetter;
import org.opentripplanner.routing.core.vehicle_sharing.Provider;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleDescription;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.updater.GraphUpdaterManager;
import org.opentripplanner.updater.PollingGraphUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SharedHistoricalVehiclesUpdater extends PollingGraphUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(SharedHistoricalVehiclesUpdater.class);

    private VehicleHistoricalPositionsGetter vehiclePositionsGetter;
    private TemporaryStreetSplitter temporaryStreetSplitter;
    private GraphUpdaterManager graphUpdaterManager;
    private Graph graph;
    private String url;
    private String password;
    private int retryWaitTimeSeconds;
    private final Map<Integer, Provider> vehicleProviders = new HashMap<>();
    private final List<SharedVehiclesSnapshotLabel> monitoredSnapshots = new ArrayList<>();

    @Override
    protected void runPolling() {
        try {
            ProvidersGetter providersGetter = new ProvidersGetter();
            while (vehicleProviders.isEmpty()) {
                vehicleProviders.putAll(providersGetter.postFromHasura(this.graph, url).stream()
                        .collect(Collectors.toMap(Provider::getProviderId, provider -> provider)));
                if (vehicleProviders.isEmpty()) {
                    LOG.error("Received empty provider list - waiting to try again");
                    Thread.sleep(TimeUnit.SECONDS.toMillis(retryWaitTimeSeconds));
                }
            }
            for (SharedVehiclesSnapshotLabel snapshotLabel : monitoredSnapshots) {
                LOG.info("Polling vehicles from API for snapshot " + snapshotLabel.getTimestamp());
                vehiclePositionsGetter = new VehicleHistoricalPositionsGetter(snapshotLabel, vehicleProviders);
                vehiclePositionsGetter.setReturnNullOnNoResponse(true);
                List<VehicleDescription> vehicles = null;
                while (Objects.isNull(vehicles)) {
                    vehicles = vehiclePositionsGetter.postFromHasuraWithPassword(this.graph, this.url, this.password);
                    if (Objects.isNull(vehicles)) {
                        LOG.error("Error occurred when connecting to the shared vehicles history API - waiting to try again");
                        Thread.sleep(TimeUnit.SECONDS.toMillis(retryWaitTimeSeconds));
                    }
                }
                LOG.info("Got {} vehicles from snapshot {} possible to place on a map", vehicles.size(), snapshotLabel);
                graphUpdaterManager.execute(new VehicleSharingGraphWriterRunnable(temporaryStreetSplitter, vehicles, null, snapshotLabel));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void configurePolling(Graph graph, JsonNode config) throws IllegalStateException {
        this.pollingPeriodSeconds = -1;
        if (config != null && config.get("pollingPeriodSeconds") != null)
            this.pollingPeriodSeconds = config.get("pollingPeriodSeconds").asInt(this.pollingPeriodSeconds);

        this.retryWaitTimeSeconds = 60;
        if (Objects.nonNull(config) && config.get("retryWaitTimeSeconds") != null)
            this.retryWaitTimeSeconds = config.get("retryWaitTimeSeconds").asInt(this.retryWaitTimeSeconds);

        this.url = Optional.ofNullable(System.getenv("SHARED_VEHICLES_HISTORY_UPDATER_API"))
                .orElseGet(() -> System.getProperty("sharedVehiclesHistoryApi"));
        if (Objects.isNull(this.url) || this.url.equals("")) {
            throw new IllegalStateException("Please provide program parameter `--sharedVehiclesHistoryApi <URL>`");
        }

        this.password = System.getenv("SHARED_VEHICLES_HISTORY_UPDATER_API_PASS");
        if (Objects.isNull(this.password) || this.password.equals("")) {
            throw new IllegalStateException("Please provide program parameter `--sharedVehiclesHistoryApiPass <password>`");
        }

        if (Objects.nonNull(config))
            configureMonitoredSnapshots(config);
    }

    private void configureMonitoredSnapshots(JsonNode config) {
        JsonNode snapshotOffsetsNode = config.get("snapshotOffsetsInDays");

        if (Objects.nonNull(snapshotOffsetsNode) && snapshotOffsetsNode.isArray()) {
            List<LocalTime> timesOfDay = getTimesOfDayFromConfig(config);
            LocalDateTime currentDate = LocalDateTime.now();

            snapshotOffsetsNode.iterator().forEachRemaining(offset -> {
                try {
                    int offsetAsInt = Integer.parseInt(offset.asText());
                    timesOfDay.forEach(timeOfDay -> monitoredSnapshots.add(
                            new SharedVehiclesSnapshotLabel(
                                    currentDate.minusDays(offsetAsInt)
                                            .withHour(timeOfDay.getHour())
                                            .withMinute(timeOfDay.getMinute())
                                            .withSecond(0).withNano(0))));
                } catch (NumberFormatException e) {
                    LOG.error("Could not parse snapshot offset: {}", offset.asText());
                }
            });
        }
    }

    private List<LocalTime> getTimesOfDayFromConfig(JsonNode config) {
        List<LocalTime> timesOfDay = new ArrayList<>();

        JsonNode snapshotTimesOfDayNode = config.get("snapshotTimesOfDay");
        if (Objects.nonNull(snapshotTimesOfDayNode) && snapshotTimesOfDayNode.isArray()) {
            snapshotTimesOfDayNode.iterator().forEachRemaining(timeOfDay -> {
                try {
                    LocalTime time = LocalTime.parse(timeOfDay.asText());
                    timesOfDay.add(time);
                } catch (DateTimeParseException e) {
                    LOG.error("Could not parse snapshot time: {}", timeOfDay.asText());
                }
            });
        }
        return timesOfDay;
    }

    @Override
    public void configure(Graph graph, JsonNode config) throws Exception {
        configurePolling(graph, config);
        type = "Shared Vehicles History";
    }

    @Override
    public void setGraphUpdaterManager(GraphUpdaterManager updaterManager) {
        this.graphUpdaterManager = updaterManager;
    }

    @Override
    public void setup(Graph graph) throws Exception {
        this.graph = graph;
        this.temporaryStreetSplitter = TemporaryStreetSplitter.createNewDefaultInstance(graph, null, null);
    }

    @Override
    public void teardown() {

    }
}
