package org.opentripplanner.api.resource;

import org.glassfish.grizzly.http.server.Request;
import org.opentripplanner.api.common.RoutingResource;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.impl.GraphPathFinder;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.scripting.api.BenchmarkResult;
import org.opentripplanner.standalone.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static org.opentripplanner.api.resource.ServerInfo.Q;

//Used for benchmarking router.
@Path("routers/{routerId}/benchmark")
public class BenchmarkDominanceResource extends RoutingResource {
    private static final Logger LOG = LoggerFactory.getLogger(BenchmarkDominanceResource.class);

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML + Q, MediaType.TEXT_XML + Q})
    public BenchmarkResult benchmark(@Context UriInfo uriInfo, @Context Request grizzlyRequest) {
        List<Double> estimatedRW = new LinkedList<>();
        List<Double> realRW = new LinkedList<>();
        List<Long> time = new LinkedList<>();
        Router router = null;
        RoutingRequest request = null;
        List<GraphPath> paths = null;
        Random generator = new Random(1);
        try {
            for (int i = 0; i < 100; i++) {
                request = super.buildRequest();

                router = otpServer.getRouter(request.routerId);
                double fromLat = request.from.lat + generator.nextDouble() * (request.to.lat - request.from.lat);
                double toLat = request.from.lat + generator.nextDouble() * (request.to.lat - request.from.lat);
                double fromLon = request.from.lng + generator.nextDouble() * (request.to.lng - request.from.lng);
                double toLon = request.from.lng + generator.nextDouble() * (request.to.lng - request.from.lng);

                RoutingRequest randomRequest = request.clone();

                randomRequest.from.lat = fromLat;
                randomRequest.from.lng = fromLon;
                randomRequest.to.lat = toLat;
                randomRequest.to.lng = toLon;

                randomRequest.setNumItineraries(1);

                GraphPathFinder gpFinder = new GraphPathFinder(router);
                try {
                    paths = gpFinder.graphPathFinderEntryPoint(request);


                    Double realW = paths.get(0).getWeight();
                    Double estimated = request.remainingWeighMultiplier *
                            request.getRoutingContext().remainingWeightHeuristic.estimateRemainingWeight(paths.get(0).states.get(0));

                    Long calculatedTime = paths.get(0).getEndTime() - paths.get(0).getStartTime();

                    realRW.add(realW);
                    estimatedRW.add(estimated);
                    time.add(calculatedTime);
                } catch (Exception e) {
                    realRW.add(-1.0);
                    estimatedRW.add(-1.0);
                    time.add(-1L);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new BenchmarkResult(realRW, estimatedRW, time);

    }
}
