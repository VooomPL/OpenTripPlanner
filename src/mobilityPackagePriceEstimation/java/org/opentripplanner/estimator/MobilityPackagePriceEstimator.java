package org.opentripplanner.estimator;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.opentripplanner.common.model.GenericLocation;
import org.opentripplanner.estimator.utils.DatabaseSnapshotDownloader;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class MobilityPackagePriceEstimator {

    private static final Logger LOG = LoggerFactory.getLogger(MobilityPackagePriceEstimator.class);
    private static final String DEFAULT_WIREMOCK_DIRECTORY = "src/mobilityPackagePriceEstimation/resources/";

    private WireMockServer wireMockServer;
    private DatabaseSnapshotDownloader databaseSnapshotDownloader;

    private Router router;
    private RoutingRequest request;
    private GraphPathFinder graphPathFinder;
    private SharedVehiclesUpdater vehiclesUpdater;

    private GenericLocation officeLocation;
    private double radius;
    private LocalDate evaluationStartDate;
    private int evaluationDaysTotal;
    private LocalTime morningHoursMin;
    private LocalTime eveningHoursMin;
    private LocalTime morningHoursMax;
    private LocalTime eveningHoursMax;
    private int snapshotIntervalInMinutes;

    public MobilityPackagePriceEstimator(EstimatorCommandLineParameters estimatorParameters) {
        this.officeLocation = new GenericLocation(estimatorParameters.getOfficeLat(), estimatorParameters.getOfficeLon());
        this.radius = estimatorParameters.getRadius();
        this.evaluationStartDate = estimatorParameters.getEvaluationStartDate();
        this.evaluationDaysTotal = estimatorParameters.getEvaluationDaysTotal();
        this.morningHoursMin = estimatorParameters.getMorningHoursMin();
        this.eveningHoursMin = estimatorParameters.getEveningHoursMin();
        this.morningHoursMax = estimatorParameters.getMorningHoursMax();
        this.eveningHoursMax = estimatorParameters.getEveningHoursMax();
        this.snapshotIntervalInMinutes = estimatorParameters.getSnapshotIntervalInMinutes();

        this.wireMockServer = new WireMockServer(options().port(8888).usingFilesUnderDirectory(DEFAULT_WIREMOCK_DIRECTORY));
        wireMockServer.start();

        this.router = InfrastructureSetupUtils.createOTPServer(estimatorParameters).getRouter(estimatorParameters.getRouterName());
        this.vehiclesUpdater = InfrastructureSetupUtils.createVehiclesUpdater(this.router);

        this.request = InfrastructureSetupUtils.createDefaultRequest();
        this.graphPathFinder = new GraphPathFinder(router);
        this.databaseSnapshotDownloader = new DatabaseSnapshotDownloader(this.router.graph, estimatorParameters.getDatabaseURL(), estimatorParameters.getDatabasePassword(), DEFAULT_WIREMOCK_DIRECTORY + "__files/");
    }

    public void estimatePrice(int requestsPerSnapshot) {
        this.databaseSnapshotDownloader.initializeProviders();

        try (BufferedWriter outputFileWriter = new BufferedWriter(new FileWriter("price_estimation.csv"))) {
            LocalDate currentSnapshotDay = evaluationStartDate;
            int dayCounter = evaluationDaysTotal;
            while (dayCounter > 0) {
                generateDailyPaths(currentSnapshotDay, morningHoursMin, morningHoursMax, false, requestsPerSnapshot, outputFileWriter);
                generateDailyPaths(currentSnapshotDay, eveningHoursMin, eveningHoursMax, true, requestsPerSnapshot, outputFileWriter);
                currentSnapshotDay = currentSnapshotDay.plusDays(1);
                dayCounter--;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateDailyPaths(LocalDate snapshotDate, LocalTime snapshotTimeMin, LocalTime snapshotTimeMax, boolean isFromOffice,
                                    int requestsPerSnapshot, BufferedWriter outputFileWriter) throws IOException {
        LocalTime currentSnapshotTime = snapshotTimeMin;
        while (!currentSnapshotTime.isAfter(snapshotTimeMax)) {
            LOG.info("Creating snapshot and computing paths for {} {}", snapshotDate, currentSnapshotTime);
            int vehiclesInSnapshot = databaseSnapshotDownloader.downloadSnapshot(LocalDateTime.of(snapshotDate, currentSnapshotTime));
            if (vehiclesInSnapshot > 0) {
                vehiclesUpdater.readFromSnapshot();
                for (int i = 0; i < requestsPerSnapshot; i++) {
                    GenericLocation workerHomeLocation = RandomLocationUtils.generateRandomLocation(officeLocation, radius);
                    BigDecimal pathPrice;
                    if (isFromOffice) {
                        pathPrice = getPathPrice(officeLocation, workerHomeLocation);
                    } else {
                        pathPrice = getPathPrice(workerHomeLocation, officeLocation);
                    }
                    LOG.info("Path price: {}", pathPrice);
                    writePathPrice(pathPrice, outputFileWriter);
                }
            }
            currentSnapshotTime = currentSnapshotTime.plusMinutes(snapshotIntervalInMinutes);
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

    private void writePathPrice(BigDecimal pathPrice, BufferedWriter outputFileWriter) throws IOException {
        if (pathPrice.compareTo(BigDecimal.ZERO) >= 0) {
            outputFileWriter.write(pathPrice + "\n");
        }
    }
}
