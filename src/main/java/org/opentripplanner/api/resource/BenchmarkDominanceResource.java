package org.opentripplanner.api.resource;

import org.glassfish.grizzly.http.server.Request;
import org.opentripplanner.api.common.RoutingResource;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.impl.GraphPathFinder;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.scripting.api.BenchmarkResult;
import org.opentripplanner.standalone.Router;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.LinkedList;
import java.util.List;

import static org.opentripplanner.api.resource.ServerInfo.Q;

public class BenchmarkDominanceResource extends RoutingResource {
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML + Q, MediaType.TEXT_XML + Q})
    public BenchmarkResult benchmark(@Context UriInfo uriInfo, @Context Request grizzlyRequest) {
        List<Double> estimatedRW = new LinkedList<>();
        List<Double> realRW = new LinkedList<>();
        Router router = null;
        RoutingRequest request = null;
        List<GraphPath> paths = null;

        try {
            /* Fill in request fields from query parameters via shared superclass method, catching any errors. */
            request = super.buildRequest();
            router = otpServer.getRouter(request.routerId);

            for (int i = 0; i < 200; i++) {
                double fromLat = request.from.lat + Math.random() * (request.to.lat - request.from.lat);
                double toLat = request.from.lat + Math.random() * (request.to.lat - request.from.lat);
                double fromLon = request.from.lng + Math.random() * (request.to.lng - request.from.lng);
                double toLon = request.from.lng + Math.random() * (request.to.lng - request.from.lng);

                RoutingRequest randomRequest = request.clone();

                randomRequest.from.lat = fromLat;
                randomRequest.from.lng = fromLon;
                randomRequest.to.lat = toLat;
                randomRequest.to.lng = toLon;

                randomRequest.numItineraries = 1;

                GraphPathFinder gpFinder = new GraphPathFinder(router); // we could also get a persistent router-scoped GraphPathFinder but there's no setup cost here
                paths = gpFinder.graphPathFinderEntryPoint(request);


                if (!paths.isEmpty()) {
                    realRW.add(paths.get(0).getWeight());
                    estimatedRW.add(paths.get(0).states.get(0).estimatedRemainingWeight);
                }


            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new BenchmarkResult(realRW, estimatedRW);

    }
}
