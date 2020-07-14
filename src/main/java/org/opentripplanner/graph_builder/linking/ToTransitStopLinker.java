package org.opentripplanner.graph_builder.linking;

import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import jersey.repackaged.com.google.common.collect.Lists;
import org.locationtech.jts.index.SpatialIndex;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.location.TemporaryStreetLocation;
import org.opentripplanner.routing.vertextype.TransitStop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.opentripplanner.graph_builder.linking.CandidateEdgesProvider.DUPLICATE_WAY_EPSILON_DEGREES;
import static org.opentripplanner.graph_builder.linking.LinkingGeoTools.RADIUS_DEG;

public class ToTransitStopLinker {

    private static final Logger LOG = LoggerFactory.getLogger(ToTransitStopLinker.class);

    private final SpatialIndex transitStopIndex;

    private final LinkingGeoTools linkingGeoTools;

    private final EdgesMaker edgesMaker;

    public ToTransitStopLinker(SpatialIndex transitStopIndex, LinkingGeoTools linkingGeoTools, EdgesMaker edgesMaker) {
        this.transitStopIndex = transitStopIndex;
        this.linkingGeoTools = linkingGeoTools;
        this.edgesMaker = edgesMaker;
    }

    public boolean tryLinkVertexToStop(Vertex vertex) {
        // We only link to stops if we are searching for origin/destination and for that we need transitStopIndex.
        if (transitStopIndex == null) {
            return false;
        }
        LOG.debug("No street edge was found for {}", vertex);
        // We search for closest stops (since this is only used in origin/destination linking if no edges were found)
        // in the same way the closest edges are found.
        List<TransitStop> candidateStops = new ArrayList<>();
        transitStopIndex.query(linkingGeoTools.createEnvelope(vertex)).forEach(candidateStop ->
                candidateStops.add((TransitStop) candidateStop)
        );

        final TIntDoubleMap stopDistances = new TIntDoubleHashMap();

        for (TransitStop t : candidateStops) {
            stopDistances.put(t.getIndex(), linkingGeoTools.distance(vertex, t));
        }

        Collections.sort(candidateStops, (o1, o2) -> {
            double diff = stopDistances.get(o1.getIndex()) - stopDistances.get(o2.getIndex());
            if (diff < 0) {
                return -1;
            }
            if (diff > 0) {
                return 1;
            }
            return 0;
        });
        if (candidateStops.isEmpty() || stopDistances.get(candidateStops.get(0).getIndex()) > RADIUS_DEG) {
            LOG.debug("Stops aren't close either!");
            return false;
        } else {
            List<TransitStop> bestStops = Lists.newArrayList();
            // Add stops until there is a break of epsilon meters.
            // we do this to enforce determinism. if there are a lot of stops that are all extremely close to each other,
            // we want to be sure that we deterministically link to the same ones every time. Any hard cutoff means things can
            // fall just inside or beyond the cutoff depending on floating-point operations.
            int i = 0;
            do {
                bestStops.add(candidateStops.get(i++));
            } while (i < candidateStops.size() &&
                    stopDistances.get(candidateStops.get(i).getIndex()) - stopDistances
                            .get(candidateStops.get(i - 1).getIndex()) < DUPLICATE_WAY_EPSILON_DEGREES);

            for (TransitStop stop : bestStops) {
                LOG.debug("Linking vertex to stop: {}", stop.getName());
                edgesMaker.makeTemporaryEdges((TemporaryStreetLocation) vertex, stop);
            }
            return true;
        }
    }
}
