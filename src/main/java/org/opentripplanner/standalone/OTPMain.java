package org.opentripplanner.standalone;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import org.opentripplanner.common.MavenVersion;
import org.opentripplanner.graph_builder.GraphBuilder;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.impl.DefaultStreetVertexIndexFactory;
import org.opentripplanner.routing.impl.GraphScanner;
import org.opentripplanner.routing.impl.MemoryGraphSource;
import org.opentripplanner.routing.services.GraphService;
import org.opentripplanner.scripting.impl.BSFOTPScript;
import org.opentripplanner.scripting.impl.OTPScript;
import org.opentripplanner.visualizer.GraphVisualizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * This is the main entry point to OpenTripPlanner. It allows both building graphs and starting up an OTP server
 * depending on command line options. OTPMain is a concrete class making it possible to construct one with custom
 * CommandLineParameters and use its graph builder construction method from web services or scripts, not just from the
 * static main function below.
 *
 * TODO still it seems fairly natural for all of these methods to be static.
 */
public class OTPMain {

    private static final Logger LOG = LoggerFactory.getLogger(OTPMain.class);

    /** ENTRY POINT: This is the main method that is called when running otp.jar from the command line. */
    public static void main(String[] args) {

        CommandLineParameters params = parseCommandLineParams(args);

        run(params);
    }

    /** Parse and validate command line parameters.
     * */
    public static CommandLineParameters parseCommandLineParams(String[] args) {
        CommandLineParameters params = new CommandLineParameters();
        try {
            JCommander jc = new JCommander(params, args);
            if (params.version) {
                System.out.println(MavenVersion.VERSION.getLongVersionString());
                System.exit(0);
            }
            if (params.help) {
                System.out.println(MavenVersion.VERSION.getShortVersionString());
                jc.setProgramName("java -Xmx[several]G -jar otp.jar");
                jc.usage();
                System.exit(0);
            }
            params.infer();
        } catch (ParameterException pex) {
            System.out.println(MavenVersion.VERSION.getShortVersionString());
            LOG.error("Parameter error: {}", pex.getMessage());
            System.exit(1);
        }

        if (params.build == null && !params.visualize && !params.server && params.scriptFile == null) {
            LOG.info("Nothing to do. Use --help to see available tasks.");
            System.exit(-1);
        }
        return params;
    }

    /**
     * Making OTPMain a concrete class and placing this logic an instance method instead of embedding it in the static
     * main method makes it possible to build graphs from web services or scripts, not just from the command line.
     *
     * @return
     *         true - if the OTPServer starts successfully. If "Run an OTP API server" has been requested, this method will return when the web server shuts down;
     *         false - if an error occurs while loading the graph;
     */
    public static OTPServer run(CommandLineParameters params) {
        /* Create the top-level objects that represent the OTP server. */
        GraphService graphService = new GraphService(params.autoReload, params.graphDirectory);
        OTPServer otpServer = new OTPServer(params, graphService);

        if (params.build != null) {
            buildGraph(params, graphService);
        }

        // FIXME eventually router IDs will be present even when just building a graph.
        if ((params.routerIds != null && params.routerIds.size() > 0) || params.autoScan) {
            registerRouters(params, graphService);
        }

        if (params.visualize) {
            visualize(graphService);
        }

        if (params.scriptFile != null) {
            startScript(params, otpServer);
        }

        if (params.server) {
            startServer(params, otpServer);
        }
        return otpServer;
    }

    /** Start graph builder */
    public static void buildGraph(CommandLineParameters params, GraphService graphService) {
        GraphBuilder graphBuilder = GraphBuilder.forDirectory(params, params.build); // TODO multiple directories
        if (graphBuilder != null) {
            graphBuilder.run();
            /* If requested, hand off the graph to the server as the default graph using an in-memory GraphSource. */
            if (params.inMemory || params.preFlight) {
                Graph graph = graphBuilder.getGraph();
                graph.index(new DefaultStreetVertexIndexFactory());
                // FIXME set true router IDs
                graphService.registerGraph("", new MemoryGraphSource("", graph));
            }
        } else {
            LOG.error("An error occurred while building the graph.");
            System.exit(-1);
        }
    }

    /** Scan for graphs to load from disk */
    public static void registerRouters(CommandLineParameters params, GraphService graphService) {
        /* Auto-register pre-existing graph on disk, with optional auto-scan. */
        GraphScanner graphScanner = new GraphScanner(graphService, params.graphDirectory, params.autoScan);
        graphScanner.basePath = params.graphDirectory;
        if (params.routerIds != null && params.routerIds.size() > 0) {
            graphScanner.defaultRouterId = params.routerIds.get(0);
        }
        graphScanner.autoRegister = params.routerIds;
        graphScanner.startup();
    }

    /** Start visualizer */
    public static void visualize(GraphService graphService) {
        Router defaultRouter = graphService.getRouter();
        defaultRouter.graphVisualizer = new GraphVisualizer(defaultRouter);
        defaultRouter.graphVisualizer.run();
        defaultRouter.routerDefaultTimeouts = new double[]{60}; // avoid timeouts due to search animation
    }

    /** Start script */
    public static void startScript(CommandLineParameters params, OTPServer otpServer) {
        try {
            OTPScript otpScript = new BSFOTPScript(otpServer, params.scriptFile);
            if (otpScript != null) {
                Object retval = otpScript.run();
                if (retval != null) {
                    LOG.warn("Your script returned something, no idea what to do with it: {}", retval);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Start web server */
    public static void startServer(CommandLineParameters params, OTPServer otpServer) {
        GrizzlyServer grizzlyServer = new GrizzlyServer(params, otpServer);
        while (true) { // Loop to restart server on uncaught fatal exceptions.
            try {
                grizzlyServer.run();
                return;
            } catch (Throwable throwable) {
                LOG.error("An uncaught {} occurred inside OTP. Restarting server.",
                        throwable.getClass().getSimpleName(), throwable);
            }
        }
    }

    /**
     * Open and parse the JSON file at the given path into a Jackson JSON tree. Comments and unquoted keys are allowed.
     * Returns null if the file does not exist,
     * Returns null if the file contains syntax errors or cannot be parsed for some other reason.
     *
     * We do not require any JSON config files to be present because that would get in the way of the simplest
     * rapid deployment workflow. Therefore we return an empty JSON node when the file is missing, causing us to fall
     * back on all the default values as if there was a JSON file present with no fields defined.
     */
    public static JsonNode loadJson (File file) {
        try (FileInputStream jsonStream = new FileInputStream(file)) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
            mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
            JsonNode config = mapper.readTree(jsonStream);
            LOG.info("Found and loaded JSON configuration file '{}'", file);
            return config;
        } catch (FileNotFoundException ex) {
            LOG.info("File '{}' is not present. Using default configuration.", file);
            return MissingNode.getInstance();
        } catch (Exception ex) {
            LOG.error("Error while parsing JSON config file '{}': {}", file, ex.getMessage());
            System.exit(42); // probably "should" be done with an exception
            return null;
        }
    }


}
