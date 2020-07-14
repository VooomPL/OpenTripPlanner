package org.opentripplanner.graph_builder.linking;

import com.google.common.collect.Iterables;
import org.opentripplanner.common.geometry.HashGridSpatialIndex;
import org.opentripplanner.graph_builder.annotation.BikeParkUnlinked;
import org.opentripplanner.graph_builder.annotation.BikeRentalStationUnlinked;
import org.opentripplanner.graph_builder.annotation.StopUnlinked;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.edgetype.*;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.vertextype.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class links transit stops to streets by splitting the streets (unless the stop is extremely close to the street
 * intersection).
 * <p>
 * It is intended to eventually completely replace the existing stop linking code, which had been through so many
 * revisions and adaptations to different street and turn representations that it was very glitchy. This new code is
 * also intended to be deterministic in linking to streets, independent of the order in which the JVM decides to
 * iterate over Maps and even in the presence of points that are exactly halfway between multiple candidate linking
 * points.
 * <p>
 * It would be wise to keep this new incarnation of the linking code relatively simple, considering what happened before.
 * <p>
 * See discussion in pull request #1922, follow up issue #1934, and the original issue calling for replacement of
 * the stop linker, #1305.
 */
public class SimpleStreetSplitter {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleStreetSplitter.class);

    protected final Graph graph;

    protected final HashGridSpatialIndex<Edge> idx;

    protected final Linker linker;

    /**
     * Construct a new SimpleStreetSplitter.
     * NOTE: Only one SimpleStreetSplitter should be active on a graph at any given time.
     *
     * @param hashGridSpatialIndex If not null this index is used instead of creating new one
     * @param linker
     */
    protected SimpleStreetSplitter(Graph graph, HashGridSpatialIndex<Edge> hashGridSpatialIndex, Linker linker) {
        this.graph = graph;
        this.linker = linker;
        //We build a spatial index if it isn't provided
        if (hashGridSpatialIndex == null) {
            // build a nice private spatial index, since we're adding and removing edges
            idx = new HashGridSpatialIndex<>();
            for (StreetEdge se : Iterables.filter(graph.getEdges(), StreetEdge.class)) {
                idx.insert(se.getGeometry(), se);
            }
        } else {
            idx = hashGridSpatialIndex;
        }
    }

    public HashGridSpatialIndex<Edge> getIdx() {
        return idx;
    }

    /**
     * Link all relevant vertices to the street network
     */
    public void link() {
        for (Vertex v : graph.getVertices()) {
            if (hasToBeLinked(v)) {
                if (!link(v)) {
                    logLinkingFailure(v);
                }
            }
        }
    }

    private boolean hasToBeLinked(Vertex v) {
        if (v instanceof TransitStop || v instanceof BikeRentalStationVertex || v instanceof BikeParkVertex) {
            return v.getOutgoing().stream().noneMatch(e -> e instanceof StreetTransitLink); // not yet linked
        }
        return false;
    }

    private void logLinkingFailure(Vertex v) {
        if (v instanceof TransitStop)
            LOG.warn(graph.addBuilderAnnotation(new StopUnlinked((TransitStop) v)));
        else if (v instanceof BikeRentalStationVertex)
            LOG.warn(graph.addBuilderAnnotation(new BikeRentalStationUnlinked((BikeRentalStationVertex) v)));
        else if (v instanceof BikeParkVertex)
            LOG.warn(graph.addBuilderAnnotation(new BikeParkUnlinked((BikeParkVertex) v)));
    }

    /**
     * Link this vertex into the graph to the closest walkable edge
     */
    public boolean link(Vertex vertex) {
        return linker.linkPermanently(vertex, TraverseMode.WALK);
    }

    public void setAddExtraEdgesToAreas(boolean addExtraEdgesToAreas) {
        this.linker.setAddExtraEdgesToAreas(addExtraEdgesToAreas);
    }
}
