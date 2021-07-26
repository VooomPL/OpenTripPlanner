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

import java.net.ConnectException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;

public class SharedHistoricalVehiclesUpdater extends PollingGraphUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(SharedHistoricalVehiclesUpdater.class);

    private VehicleHistoricalPositionsGetter vehiclePositionsGetter;
    private TemporaryStreetSplitter temporaryStreetSplitter;
    private GraphUpdaterManager graphUpdaterManager;
    private Graph graph;
    private String url;
    private String password;
    private int retryWaitTimeSeconds;
    private int maxRetries;
    private final Map<Integer, Provider> vehicleProviders = new HashMap<>();

    @Override
    protected void runPolling() {
        try {
            int retryCounter = 0;
            ProvidersGetter providersGetter = new ProvidersGetter();
            while (vehicleProviders.isEmpty()) {
                vehicleProviders.putAll(providersGetter.postFromHasura(this.graph, url).stream()
                        .collect(Collectors.toMap(Provider::getProviderId, provider -> provider)));
                if (vehicleProviders.isEmpty()) {
                    LOG.error("Received empty provider list - waiting to try again");
                    if (maxRetries != -1) {
                        retryCounter++;
                        if (isRetryLimitExceeded(retryCounter))
                            break;
                    }
                    Thread.sleep(TimeUnit.SECONDS.toMillis(retryWaitTimeSeconds));
                }
            }
            if (!vehicleProviders.isEmpty()) {
                for (SharedVehiclesSnapshotLabel snapshotLabel : graph.getSupportedSnapshotLabels().keySet()) {
                    LOG.info("Polling vehicles from API for snapshot " + snapshotLabel.getTimestamp());
                    retryCounter = 0;
                    vehiclePositionsGetter = new VehicleHistoricalPositionsGetter(snapshotLabel, vehicleProviders);
                    List<VehicleDescription> vehicles = null;
                    while (Objects.isNull(vehicles)) {
                        vehicles = vehiclePositionsGetter.postFromHasuraWithPassword(this.graph, this.url, this.password);
                        if (Objects.isNull(vehicles)) {
                            LOG.error("Error occurred when connecting to the shared vehicles history API to download " +
                                    "snapshot {} - waiting to try again", snapshotLabel);
                            if (maxRetries != -1) {
                                retryCounter++;
                                if (isRetryLimitExceeded(retryCounter))
                                    break;
                            }
                            Thread.sleep(TimeUnit.SECONDS.toMillis(retryWaitTimeSeconds));
                        }
                    }
                    if (Objects.nonNull(vehicles)) {
                        LOG.info("Got {} vehicles from snapshot {} possible to place on a map", vehicles.size(), snapshotLabel);
                        graphUpdaterManager.execute(new VehicleSharingGraphWriterRunnable(temporaryStreetSplitter, vehicles, null, snapshotLabel));
                    } else {
                        LOG.error("Could not download vehicle positions snapshot for timestamp: {}", snapshotLabel);
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean isRetryLimitExceeded(int retryCounter) {
        return maxRetries != -1 && retryCounter > maxRetries;
    }

    @Override
    protected void configurePolling(Graph graph, JsonNode config) throws IllegalStateException {
        this.pollingPeriodSeconds = -1;
        if (config != null && config.get("pollingPeriodSeconds") != null)
            this.pollingPeriodSeconds = config.get("pollingPeriodSeconds").asInt(this.pollingPeriodSeconds);

        this.retryWaitTimeSeconds = 60;
        if (Objects.nonNull(config) && config.get("retryWaitTimeSeconds") != null)
            this.retryWaitTimeSeconds = config.get("retryWaitTimeSeconds").asInt(this.retryWaitTimeSeconds);

        this.maxRetries = -1;
        if (Objects.nonNull(config) && config.get("maxRetries") != null)
            this.maxRetries = config.get("maxRetries").asInt(this.maxRetries);

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
            configureMonitoredSnapshots(config, graph);
    }

    private void configureMonitoredSnapshots(JsonNode config, Graph graph) {
        JsonNode snapshotOffsetsNode = config.get("snapshotOffsetsInDays");

        if (Objects.nonNull(snapshotOffsetsNode) && snapshotOffsetsNode.isArray()) {
            List<LocalTime> timesOfDay = getTimesOfDayFromConfig(config);
            LocalDateTime currentDate = LocalDateTime.now();

            snapshotOffsetsNode.iterator().forEachRemaining(offset -> {
                try {
                    int offsetAsInt = Integer.parseInt(offset.asText());
                    LocalDateTime snapshotDate = computeSnapshotDate(currentDate, offsetAsInt);
                    timesOfDay.forEach(timeOfDay -> {
                        graph.getSupportedSnapshotLabels().put(new SharedVehiclesSnapshotLabel(
                                snapshotDate.withHour(timeOfDay.getHour())
                                        .withMinute(timeOfDay.getMinute())
                                        .withSecond(0).withNano(0)), -1);
                    });
                } catch (NumberFormatException e) {
                    LOG.error("Could not parse snapshot offset: {}", offset.asText());
                }
            });
        }
    }

    private LocalDateTime computeSnapshotDate(LocalDateTime currentDate, int offset) {
        LocalDateTime snapshotDate = currentDate.minusDays(offset);
        DayOfWeek snapshotDayOfWeek = snapshotDate.getDayOfWeek();
        if (snapshotDayOfWeek == SATURDAY || snapshotDayOfWeek == SUNDAY) {
            return snapshotDate.minusDays(2);
        }
        return snapshotDate;
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
