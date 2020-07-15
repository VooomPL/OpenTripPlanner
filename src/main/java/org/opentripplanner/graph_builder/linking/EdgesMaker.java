package org.opentripplanner.graph_builder.linking;

import com.google.common.collect.Iterables;
import org.opentripplanner.routing.edgetype.StreetBikeParkLink;
import org.opentripplanner.routing.edgetype.StreetBikeRentalLink;
import org.opentripplanner.routing.edgetype.StreetTransitLink;
import org.opentripplanner.routing.edgetype.TemporaryFreeEdge;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.location.TemporaryStreetLocation;
import org.opentripplanner.routing.vertextype.BikeParkVertex;
import org.opentripplanner.routing.vertextype.BikeRentalStationVertex;
import org.opentripplanner.routing.vertextype.StreetVertex;
import org.opentripplanner.routing.vertextype.TransitStop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EdgesMaker {

    private static final Logger LOG = LoggerFactory.getLogger(EdgesMaker.class);

    /**
     * Make temporary edges to origin/destination vertex in origin/destination search
     **/
    public void makeTemporaryEdges(TemporaryStreetLocation from, Vertex to) {
        if (from.isEndVertex()) {
            LOG.debug("Linking end vertex to {} -> {}", to, from);
            new TemporaryFreeEdge(to, from);
        } else {
            LOG.debug("Linking start vertex to {} -> {}", from, to);
            new TemporaryFreeEdge(from, to);
        }
    }

    public void makePermanentEdges(Vertex from, StreetVertex to) {
        if (from instanceof TransitStop) {
            makeTransitLinkEdges((TransitStop) from, to);
        } else if (from instanceof BikeRentalStationVertex) {
            makeBikeRentalLinkEdges((BikeRentalStationVertex) from, to);
        } else if (from instanceof BikeParkVertex) {
            makeBikeParkEdges((BikeParkVertex) from, to);
        } else {
            LOG.warn("Not supported type of vertex: {}", from.getClass());
        }
    }

    /**
     * Make street transit link edges, unless they already exist.
     */
    private void makeTransitLinkEdges(TransitStop tstop, StreetVertex v) {
        // ensure that the requisite edges do not already exist
        // this can happen if we link to duplicate ways that have the same start/end vertices.
        for (StreetTransitLink e : Iterables.filter(tstop.getOutgoing(), StreetTransitLink.class)) {
            if (e.getToVertex() == v)
                return;
        }

        new StreetTransitLink(tstop, v, tstop.hasWheelchairEntrance());
        new StreetTransitLink(v, tstop, tstop.hasWheelchairEntrance());
    }

    /**
     * Make link edges for bike rental
     */
    private void makeBikeRentalLinkEdges(BikeRentalStationVertex from, StreetVertex to) {
        for (StreetBikeRentalLink sbrl : Iterables.filter(from.getOutgoing(), StreetBikeRentalLink.class)) {
            if (sbrl.getToVertex() == to)
                return;
        }

        new StreetBikeRentalLink(from, to);
        new StreetBikeRentalLink(to, from);
    }

    /**
     * Make bike park edges
     */
    private void makeBikeParkEdges(BikeParkVertex from, StreetVertex to) {
        for (StreetBikeParkLink sbpl : Iterables.filter(from.getOutgoing(), StreetBikeParkLink.class)) {
            if (sbpl.getToVertex() == to)
                return;
        }

        new StreetBikeParkLink(from, to);
        new StreetBikeParkLink(to, from);
    }
}
