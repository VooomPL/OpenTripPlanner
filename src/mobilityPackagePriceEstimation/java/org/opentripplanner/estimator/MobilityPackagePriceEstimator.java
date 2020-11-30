package org.opentripplanner.estimator;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.opentripplanner.common.model.GenericLocation;
import org.opentripplanner.estimator.utils.InfrastructureSetupUtils;
import org.opentripplanner.estimator.utils.RandomLocationUtils;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.impl.GraphPathFinder;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.standalone.Router;
import org.opentripplanner.updater.vehicle_sharing.vehicles_positions.SharedVehiclesUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class MobilityPackagePriceEstimator {

    private static final Logger LOG = LoggerFactory.getLogger(MobilityPackagePriceEstimator.class);

    private WireMockServer wireMockServer;
    private Router router;
    private RoutingRequest request;
    private GraphPathFinder graphPathFinder;
    private SharedVehiclesUpdater vehiclesUpdater;

    private GenericLocation officeLocation;
    private LocalDate evaluationStartDate = LocalDate.now().minusDays(3);
    private int evaluationDaysTotal = 1;
    private LocalTime morningHoursMin = LocalTime.now();
    private LocalTime eveningHoursMin = LocalTime.now().plusHours(6);
    private LocalTime morningHoursMax = LocalTime.now().plusHours(1);
    private LocalTime eveningHoursMax = LocalTime.now().plusHours(7);
    private int snapshotIntervalInMinutes = 61;

    public MobilityPackagePriceEstimator(EstimatorCommandLineParameters estimatorParameters) {
        this.officeLocation = new GenericLocation(estimatorParameters.getOfficeLat(), estimatorParameters.getOfficeLon());

        this.wireMockServer = new WireMockServer(options().port(8888).usingFilesUnderDirectory("src/mobilityPackagePriceEstimation/resources/"));
        wireMockServer.start();

        this.router = InfrastructureSetupUtils.createOTPServer(estimatorParameters).getRouter(estimatorParameters.getRouterName());
        System.setProperty("sharedVehiclesApi", "http://localhost:8888/query_db");

        this.vehiclesUpdater = InfrastructureSetupUtils.createVehiclesUpdater(this.router);
        this.request = InfrastructureSetupUtils.createDefaultRequest();
        this.graphPathFinder = new GraphPathFinder(router);
    }

    public void estimatePrice(int requestsPerScenario) {
        try (BufferedWriter gatheredData = new BufferedWriter(new FileWriter("results.csv"))) {
            LocalDate currentSnapshotDay = evaluationStartDate;
            int dayCounter = evaluationDaysTotal;
            while (dayCounter > 0) {
                LocalTime currentSnapshotTime = morningHoursMin;
                while (!currentSnapshotTime.isAfter(morningHoursMax)) {
                    LOG.info("Creating snapshot and computing morning paths");
                    //DatabaseSnapshotDownloader.downloadSnapshot(LocalDateTime.now().minus(1, ChronoUnit.HOURS));
                    vehiclesUpdater.runSinglePolling();
                    //TODO: check if there are any vehicles left (snapshot for this date may not exist)
                    for (int i = 0; i < requestsPerScenario; i++) {
                        GenericLocation workerHomeLocation = RandomLocationUtils.generateRandomLocation(officeLocation, 0.01);
                        BigDecimal pathPrice = getPathPrice(workerHomeLocation, officeLocation);
                        LOG.info("Path price: {}", pathPrice);
                        if (pathPrice.compareTo(BigDecimal.ZERO) >= 0) {
                            gatheredData.write(pathPrice + "\n");
                        }
                    }
                    currentSnapshotTime = currentSnapshotTime.plusMinutes(snapshotIntervalInMinutes);
                }
                currentSnapshotTime = eveningHoursMin;
                while (!currentSnapshotTime.isAfter(eveningHoursMax)) {
                    LOG.info("Creating snapshot and computing evening paths");
                    //DatabaseSnapshotDownloader.downloadSnapshot(LocalDateTime.now().minus(1, ChronoUnit.HOURS));
                    vehiclesUpdater.runSinglePolling();
                    //TODO: check if there are any vehicles left (snapshot for this date may not exist)
                    for (int i = 0; i < requestsPerScenario; i++) {
                        GenericLocation workerHomeLocation = RandomLocationUtils.generateRandomLocation(officeLocation, 0.01);
                        BigDecimal pathPrice = getPathPrice(officeLocation, workerHomeLocation);
                        LOG.info("Path price: {}", pathPrice);
                        if (pathPrice.compareTo(BigDecimal.ZERO) >= 0) {
                            gatheredData.write(pathPrice + "\n");
                        }
                    }
                    currentSnapshotTime = currentSnapshotTime.plusMinutes(snapshotIntervalInMinutes);
                }
                currentSnapshotDay = currentSnapshotDay.plusDays(1);
                dayCounter--;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BigDecimal getPathPrice(GenericLocation origin, GenericLocation destination) {
        request.from.lat = origin.lat;
        request.from.lng = origin.lng;
        request.to.lat = destination.lat;
        request.to.lng = destination.lng;

        List<GraphPath> paths = graphPathFinder.graphPathFinderEntryPoint(request);

        if (paths.size() == 0) {
            return BigDecimal.valueOf(-1);
        } else {
            return paths.get(0).states.getLast().getTraversalPrice();
        }
    }
}
