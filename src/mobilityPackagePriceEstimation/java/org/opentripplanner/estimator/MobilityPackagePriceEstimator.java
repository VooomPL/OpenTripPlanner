package org.opentripplanner.estimator;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.opentripplanner.common.model.GenericLocation;
import org.opentripplanner.estimator.utils.DatabaseSnapshotDownloader;
import org.opentripplanner.estimator.utils.RandomLocationGenerator;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.routing.impl.GraphPathFinder;
import org.opentripplanner.routing.services.GraphService;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.standalone.CommandLineParameters;
import org.opentripplanner.standalone.OTPMain;
import org.opentripplanner.standalone.OTPServer;
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
import java.util.Random;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class MobilityPackagePriceEstimator {

    private static final Logger LOG = LoggerFactory.getLogger(MobilityPackagePriceEstimator.class);

    private static final String DEFAULT_WIREMOCK_DIRECTORY = "src/mobilityPackagePriceEstimation/resources/";
    private static final int DEFAULT_WIREMOCK_PORT = 8888;
    private static final String DEFAULT_SHARED_VEHICLES_API = "http://localhost:8888/query_db";
    private static final int DEFAULT_OTP_PORT = 9111;
    private static final String DEFAULT_OUTPUT_FILE_NAME = "price_estimation.csv";

    private static final String[] otpDefaultArgs = new String[]{
            "--basePath",
            "./src/mobilityPackagePriceEstimation/resources/",
            "--inMemory",
            "--port",
            "",
            "--router",
            ""
    };

    private WireMockServer wireMockServer;
    private DatabaseSnapshotDownloader databaseSnapshotDownloader;

    private Router router;
    private RoutingRequest request;
    private GraphPathFinder graphPathFinder;
    private SharedVehiclesUpdater vehiclesUpdater;

    private GenericLocation officeLocation;
    private RandomLocationGenerator locationGenerator;
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
        this.locationGenerator = new RandomLocationGenerator(new Random());
        this.radius = estimatorParameters.getRadius();
        this.evaluationStartDate = estimatorParameters.getEvaluationStartDate();
        this.evaluationDaysTotal = estimatorParameters.getEvaluationDaysTotal();
        this.morningHoursMin = estimatorParameters.getMorningHoursMin();
        this.eveningHoursMin = estimatorParameters.getEveningHoursMin();
        this.morningHoursMax = estimatorParameters.getMorningHoursMax();
        this.eveningHoursMax = estimatorParameters.getEveningHoursMax();
        this.snapshotIntervalInMinutes = estimatorParameters.getSnapshotIntervalInMinutes();

        this.wireMockServer = new WireMockServer(options().port(DEFAULT_WIREMOCK_PORT).usingFilesUnderDirectory(DEFAULT_WIREMOCK_DIRECTORY));
        wireMockServer.start();

        this.router = createOTPServer(estimatorParameters.getRouterName(), DEFAULT_OTP_PORT).getRouter(estimatorParameters.getRouterName());
        this.vehiclesUpdater = createVehiclesUpdater(this.router);

        this.request = createDefaultRequest();
        this.graphPathFinder = new GraphPathFinder(router);
        this.databaseSnapshotDownloader = new DatabaseSnapshotDownloader(this.router.graph, estimatorParameters.getDatabaseURL(), estimatorParameters.getDatabasePassword(), DEFAULT_WIREMOCK_DIRECTORY + "__files/");
    }

    private static RoutingRequest createDefaultRequest() {
        RoutingRequest request = new RoutingRequest();

        request.startingMode = TraverseMode.WALK;
        request.modes = new TraverseModeSet("WALK,CAR,BICYCLE");
        request.rentingAllowed = true;
        request.softWalkLimiting = false;
        request.setNumItineraries(1);

        return request;
    }

    private static SharedVehiclesUpdater createVehiclesUpdater(Router router) {
        SharedVehiclesUpdater vehiclesUpdater = new SharedVehiclesUpdater();
        System.setProperty("sharedVehiclesApi", DEFAULT_SHARED_VEHICLES_API);
        try {
            vehiclesUpdater.setup(router.graph);
            vehiclesUpdater.configure(router.graph, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return vehiclesUpdater;
    }

    private static OTPServer createOTPServer(String routerName, int serverPort) {
        otpDefaultArgs[otpDefaultArgs.length - 3] = "" + serverPort;
        otpDefaultArgs[otpDefaultArgs.length - 1] = routerName;
        CommandLineParameters params = OTPMain.parseCommandLineParams(otpDefaultArgs);
        GraphService graphService = new GraphService(false, params.graphDirectory);
        OTPServer otpServer = new OTPServer(params, graphService);
        OTPMain.registerRouters(params, graphService);

        return otpServer;
    }

    public void estimatePrice(int requestsPerSnapshot) {
        this.databaseSnapshotDownloader.initializeProviders();

        try (BufferedWriter outputFileWriter = new BufferedWriter(new FileWriter(DEFAULT_OUTPUT_FILE_NAME))) {
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
                    GenericLocation workerHomeLocation = locationGenerator.generateRandomLocation(officeLocation, radius);
                    BigDecimal pathPrice;
                    if (isFromOffice) {
                        pathPrice = getPathPrice(officeLocation, workerHomeLocation);
                    } else {
                        pathPrice = getPathPrice(workerHomeLocation, officeLocation);
                    }
                    LOG.info("Path price: {}", pathPrice);
                    writePathPriceToFile(pathPrice, outputFileWriter);
                }
            } else {
                LOG.warn("No valid vehicles in snapshot from {} {}", snapshotDate, currentSnapshotTime);
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

    private void writePathPriceToFile(BigDecimal pathPrice, BufferedWriter outputFileWriter) throws IOException {
        if (pathPrice.compareTo(BigDecimal.ZERO) >= 0) {
            outputFileWriter.write(pathPrice + "\n");
        }
    }
}
