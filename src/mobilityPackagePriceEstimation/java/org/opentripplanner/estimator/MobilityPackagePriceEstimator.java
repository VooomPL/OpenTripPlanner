package org.opentripplanner.estimator;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.ContentPattern;
import org.opentripplanner.common.model.GenericLocation;
import org.opentripplanner.estimator.utils.RandomLocationUtils;
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

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class MobilityPackagePriceEstimator {

    private static final Logger LOG = LoggerFactory.getLogger(MobilityPackagePriceEstimator.class);

    private static final int OTP_PORT = 9111;

    private String[] otpArgs = new String[]{
            "--basePath",
            "./src/mobilityPackagePriceEstimation/resources/",
            "--inMemory",
            "--port",
            Integer.toString(OTP_PORT),
            "--router",
            ""
    };

    private WireMockServer wireMockServer;
    private Router router;
    private RoutingRequest request;
    private GraphPathFinder graphPathFinder;
    private SharedVehiclesUpdater vehiclesUpdater;
    private GenericLocation officeLocation;

    public MobilityPackagePriceEstimator(EstimatorCommandLineParameters estimatorParameters) {
        //TODO: the problem is, that depending on the office location more or less random points may be outside the city
        this.officeLocation = new GenericLocation(estimatorParameters.getOfficeLat(), estimatorParameters.getOfficeLon());
        //TODO: divide it into separate methods for initialization of different components!
        this.wireMockServer = new WireMockServer(options().port(8888).usingFilesUnderDirectory("src/mobilityPackagePriceEstimation/resources/"));
        wireMockServer.start();
        LOG.info("Mock API server started!");

        this.otpArgs[this.otpArgs.length - 1] = estimatorParameters.getRouterName();
        CommandLineParameters params = OTPMain.parseCommandLineParams(this.otpArgs);
        GraphService graphService = new GraphService(false, params.graphDirectory);
        OTPServer otpServer = new OTPServer(params, graphService);

        OTPMain.registerRouters(params, graphService);

        this.router = otpServer.getRouter(estimatorParameters.getRouterName());

        this.vehiclesUpdater = new SharedVehiclesUpdater();
        try {
            vehiclesUpdater.setup(router.graph);
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.request = new RoutingRequest();
        this.request.startingMode = TraverseMode.WALK;
        //TODO: should check different mode settings within a single pair od origin->destination?
        this.request.modes = new TraverseModeSet("WALK,CAR,BICYCLE");
        this.request.rentingAllowed = true;
        this.request.softWalkLimiting = false;
        this.request.setNumItineraries(1);
        this.graphPathFinder = new GraphPathFinder(router);
    }

    public void estimatePrice(int requestsPerScenario) {
        Set<String> toOfficeScenarios = new HashSet<>();
        Set<String> toHomeScenarios = new HashSet<>();

        wireMockServer.getStubMappings().forEach(stubMapping -> {
            List<ContentPattern<?>> bodyPatterns = stubMapping.getRequest().getBodyPatterns();
            if (Objects.nonNull(bodyPatterns) && bodyPatterns.size() > 0 && bodyPatterns.get(0).getExpected().equals("query VehiclesForArea")) {
                String urlPattern = stubMapping.getRequest().getUrlPath();
                if (urlPattern.startsWith("/morning")) {
                    toOfficeScenarios.add(urlPattern);
                } else {
                    toHomeScenarios.add(urlPattern);
                }
            }
        });
        //TODO: Refactor this not to repeat the same loop twice - detect whether this is morning or evening using URL pattern!!!
        for (String apiUrl : toOfficeScenarios) {
            LOG.info("Starting to office scenario - API URL {}", apiUrl);
            setApiUrl(apiUrl);
            LOG.info("API URL updated!");

            for (int i = 0; i < requestsPerScenario; i++) {
                GenericLocation workerHomeLocation = RandomLocationUtils.generateRandomLocation(officeLocation, 0.01);
                BigDecimal pathPrice = getPathPrice(workerHomeLocation, officeLocation);
                //TODO: deal with the PathNotFoundException (Couldn't link 53.03073782359882,19.218650531405732), when random point is outside the graph
                LOG.info("Path price: {}", pathPrice);
                //TODO: Add statistics modification!
            }
        }

        for (String apiUrl : toHomeScenarios) {
            LOG.info("Starting to home scenario - api URL {}", apiUrl);
            setApiUrl(apiUrl);
            LOG.info("API URL updated!");

            for (int i = 0; i < requestsPerScenario; i++) {
                GenericLocation workerHomeLocation = RandomLocationUtils.generateRandomLocation(officeLocation, 0.01);
                BigDecimal pathPrice = getPathPrice(officeLocation, workerHomeLocation);
                //TODO: deal with the PathNotFoundException (Couldn't link 53.03073782359882,19.218650531405732), when random point is outside the graph
                LOG.info("Path price: {}", pathPrice);
                //TODO: Add statistics modification!
            }
        }

        System.exit(0);
    }

    public void setApiUrl(String apiUrl) {
        try {
            System.setProperty("sharedVehiclesApi", "http://localhost:8888" + apiUrl);
            //TODO: Add updater for bike stations as well
            vehiclesUpdater.configure(router.graph, null);
            vehiclesUpdater.runSinglePolling();
        } catch (Exception e) {
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
