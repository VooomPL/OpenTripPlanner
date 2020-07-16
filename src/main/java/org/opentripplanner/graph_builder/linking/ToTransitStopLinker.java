package org.opentripplanner.graph_builder.linking;

import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import org.locationtech.jts.index.SpatialIndex;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.location.TemporaryStreetLocation;
import org.opentripplanner.routing.vertextype.TransitStop;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.opentripplanner.graph_builder.linking.LinkingGeoTools.RADIUS_DEG;

public class ToTransitStopLinker {

    private final SpatialIndex transitStopIndex;

    private final LinkingGeoTools linkingGeoTools;

    private final EdgesMaker edgesMaker;

    public ToTransitStopLinker(SpatialIndex transitStopIndex, LinkingGeoTools linkingGeoTools, EdgesMaker edgesMaker) {
        this.transitStopIndex = transitStopIndex;
        this.linkingGeoTools = linkingGeoTools;
        this.edgesMaker = edgesMaker;
    }

    public boolean tryLinkVertexToStop(TemporaryStreetLocation vertex) {
        List<TransitStop> transitStops = findTransitStopsToLink(vertex);
        transitStops.forEach(stop -> edgesMaker.makeTemporaryEdges(vertex, stop));
        return !transitStops.isEmpty();
    }

    private List<TransitStop> findTransitStopsToLink(Vertex vertex) {
        List<TransitStop> candidateStops = getCandidateStops(vertex);
        TIntDoubleMap stopDistances = getDistances(candidateStops, vertex);
        if (candidateStops.isEmpty() || stopDistances.get(candidateStops.get(0).getIndex()) > RADIUS_DEG) {
            return emptyList();
        }
        EdgesFinderUtils.sort(candidateStops, stopDistances, Vertex::getIndex);
        return EdgesFinderUtils.getBestCandidates(candidateStops, stopDistances, Vertex::getIndex);
    }

    private List<TransitStop> getCandidateStops(Vertex vertex) {
        // We only link to stops if we are searching for origin/destination and for that we need transitStopIndex.
        if (transitStopIndex == null) {
            return emptyList();
        }
        // We search for closest stops (since this is only used in origin/destination linking if no edges were found)
        // in the same way the closest edges are found.
        return ((List<Vertex>) transitStopIndex.query(linkingGeoTools.createEnvelope(vertex))).stream()
                .filter(TransitStop.class::isInstance)
                .map(TransitStop.class::cast)
                .collect(toList());
    }

    private TIntDoubleMap getDistances(List<TransitStop> candidateStops, Vertex vertex) {
        TIntDoubleMap stopDistances = new TIntDoubleHashMap();
        candidateStops.forEach(t -> stopDistances.put(t.getIndex(), linkingGeoTools.distance(vertex, t)));
        return stopDistances;
    }
}
