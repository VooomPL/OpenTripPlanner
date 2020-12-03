package org.opentripplanner.estimator.utils;

import org.opentripplanner.estimator.EstimatorCommandLineParameters;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.routing.services.GraphService;
import org.opentripplanner.standalone.CommandLineParameters;
import org.opentripplanner.standalone.OTPMain;
import org.opentripplanner.standalone.OTPServer;
import org.opentripplanner.standalone.Router;
import org.opentripplanner.updater.vehicle_sharing.vehicles_positions.SharedVehiclesUpdater;

public class InfrastructureSetupUtils {

    //TODO: do sth about the fixed port number?
    private static final int OTP_PORT_DEFAULT_PORT = 9111;

    private static final String[] otpDefaultArgs = new String[]{
            "--basePath",
            "./src/mobilityPackagePriceEstimation/resources/",
            "--inMemory",
            "--port",
            Integer.toString(OTP_PORT_DEFAULT_PORT),
            "--router",
            ""
    };

    public static RoutingRequest createDefaultRequest() {
        RoutingRequest request = new RoutingRequest();

        request.startingMode = TraverseMode.WALK;
        request.modes = new TraverseModeSet("WALK,CAR,BICYCLE");
        request.rentingAllowed = true;
        request.softWalkLimiting = false;
        request.setNumItineraries(1);

        return request;
    }

    public static SharedVehiclesUpdater createVehiclesUpdater(Router router) {
        SharedVehiclesUpdater vehiclesUpdater = new SharedVehiclesUpdater();
        System.setProperty("sharedVehiclesApi", "http://localhost:8888/query_db");
        try {
            vehiclesUpdater.setup(router.graph);
            vehiclesUpdater.configure(router.graph, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return vehiclesUpdater;
    }

    public static OTPServer createOTPServer(EstimatorCommandLineParameters estimatorParameters) {
        otpDefaultArgs[otpDefaultArgs.length - 1] = estimatorParameters.getRouterName();
        CommandLineParameters params = OTPMain.parseCommandLineParams(otpDefaultArgs);
        GraphService graphService = new GraphService(false, params.graphDirectory);
        OTPServer otpServer = new OTPServer(params, graphService);
        OTPMain.registerRouters(params, graphService);

        return otpServer;
    }
}
