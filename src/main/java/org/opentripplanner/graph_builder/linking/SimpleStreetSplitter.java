package org.opentripplanner.graph_builder.linking;

import com.google.common.collect.Iterables;
import gnu.trove.map.TIntDoubleMap;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.linearref.LinearLocation;
import org.opentripplanner.common.geometry.HashGridSpatialIndex;
import org.opentripplanner.common.geometry.SphericalDistanceLibrary;
import org.opentripplanner.common.model.P2;
import org.opentripplanner.graph_builder.annotation.BikeParkUnlinked;
import org.opentripplanner.graph_builder.annotation.BikeRentalStationUnlinked;
import org.opentripplanner.graph_builder.annotation.StopLinkedTooFar;
import org.opentripplanner.graph_builder.annotation.StopUnlinked;
import org.opentripplanner.graph_builder.services.DefaultStreetEdgeFactory;
import org.opentripplanner.graph_builder.services.StreetEdgeFactory;
import org.opentripplanner.openstreetmap.model.OSMWithTags;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.edgetype.*;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.location.TemporaryStreetLocation;
import org.opentripplanner.routing.vertextype.*;
import org.opentripplanner.util.I18NString;
import org.opentripplanner.util.LocalizedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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
public class SimpleStreetSplitter extends StreetSplitter {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleStreetSplitter.class);

    private static final int WARNING_DISTANCE_METERS = 20;

    private Boolean addExtraEdgesToAreas = false;

    private final StreetEdgeFactory edgeFactory = new DefaultStreetEdgeFactory();

    /**
     * Construct a new SimpleStreetSplitter. Be aware that only one SimpleStreetSplitter should be
     * active on a graph at any given time.
     * <p>
     * SimpleStreetSplitter generates index on graph and splits destructively (used in transit splitter)
     *
     * @param graph
     */
    public SimpleStreetSplitter(Graph graph) {
        super(graph, null);
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
        return link(vertex, TraverseMode.WALK);
    }

    /**
     * Make the appropriate type of link edges from a vertex
     */
    @Override
    protected void makeLinkEdges(Vertex from, StreetVertex to) {
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
     * split the edge and link in the transit stop
     */
    private void link(Vertex tstop, StreetEdge edge, double xscale) {
        // TODO: we've already built this line string, we should save it
        LineString orig = edge.getGeometry();
        LinearLocation ll = createLinearLocation(tstop, orig, xscale);
        if (tryLinkVertexToVertex(tstop, edge, orig, ll)) {
            return;
        }
        linkVertexOnEdge(tstop, edge, ll);
    }

    private void linkVertexOnEdge(Vertex tstop, StreetEdge edge, LinearLocation ll) {
        // split the edge, get the split vertex
        SplitterVertex v0 = split(edge, ll);
        makeLinkEdges(tstop, v0);

        // If splitter vertex is part of area; link splittervertex to all other vertexes in area, this creates
        // edges that were missed by WalkableAreaBuilder
        if (edge instanceof AreaEdge && tstop instanceof TransitStop && this.addExtraEdgesToAreas) {
            linkTransitToAreaVertices(v0, ((AreaEdge) edge).getArea());
        }
    }


    // Link to all vertices in area/platform
    protected void linkTransitToAreaVertices(Vertex splitterVertex, AreaEdgeList area) {
        List<Vertex> vertices = new ArrayList<>();

        for (AreaEdge areaEdge : area.getEdges()) {
            if (!vertices.contains(areaEdge.getToVertex())) vertices.add(areaEdge.getToVertex());
            if (!vertices.contains(areaEdge.getFromVertex())) vertices.add(areaEdge.getFromVertex());
        }

        for (Vertex vertex : vertices) {
            if (vertex instanceof StreetVertex && !vertex.equals(splitterVertex)) {
                LineString line = geometryFactory.createLineString(new Coordinate[]{splitterVertex.getCoordinate(), vertex.getCoordinate()});
                double length = SphericalDistanceLibrary.distance(splitterVertex.getCoordinate(),
                        vertex.getCoordinate());
                I18NString name = new LocalizedString("", new OSMWithTags());

                edgeFactory.createAreaEdge((IntersectionVertex) splitterVertex, (IntersectionVertex) vertex, line, name, length, StreetTraversalPermission.PEDESTRIAN_AND_BICYCLE, false, area);
                edgeFactory.createAreaEdge((IntersectionVertex) vertex, (IntersectionVertex) splitterVertex, line, name, length, StreetTraversalPermission.PEDESTRIAN_AND_BICYCLE, false, area);
            }
        }
    }


    /**
     * Split the street edge at the given fraction
     *
     * @param edge           to be split
     * @param ll             fraction at which to split the edge
     * @return Splitter vertex with added new edges
     */
    private SplitterVertex split(StreetEdge edge, LinearLocation ll) {
        LineString geometry = edge.getGeometry();

        // create the geometries
        Coordinate splitPoint = ll.getCoordinate(geometry);

        // every edge can be split exactly once, so this is a valid label
        SplitterVertex v = new SplitterVertex(graph, "split from " + edge.getId(), splitPoint.x, splitPoint.y, edge);

        // Split the 'edge' at 'v' in 2 new edges and connect these 2 edges to the
        // existing vertices
        P2<StreetEdge> edges = edge.split(v, true);

        // update indices of new edges
        idx.insert(edges.first.getGeometry(), edges.first);
        idx.insert(edges.second.getGeometry(), edges.second);

        // (no need to remove original edge, we filter it when it comes out of the index)

        // remove original edge from the graph
        edge.getToVertex().removeIncoming(edge);
        edge.getFromVertex().removeOutgoing(edge);

        return v;
    }

    /**
     * Link this vertex into the graph
     */
    protected boolean link(Vertex vertex, TraverseMode traverseMode) {
        // find nearby street edges
        // TODO: we used to use an expanding-envelope search, which is more efficient in
        // dense areas. but first let's see how inefficient this is. I suspect it's not too
        // bad and the gains in simplicity are considerable.

        Envelope env = new Envelope(vertex.getCoordinate());

        // Perform a simple local equirectangular projection, so distances are expressed in degrees latitude.
        double xscale = Math.cos(vertex.getLat() * Math.PI / 180);

        // Expand more in the longitude direction than the latitude direction to account for converging meridians.
        env.expandBy(RADIUS_DEG / xscale, RADIUS_DEG);

        List<StreetEdge> candidateEdges = getCandidateEdges(env, traverseMode);

        // Make a map of distances to all edges.
        TIntDoubleMap distances = getDistances(candidateEdges, vertex, xscale);

        sortCandidateEdges(candidateEdges, distances);

        // find the closest candidate edges
        if (candidateEdges.isEmpty() || distances.get(candidateEdges.get(0).getId()) > RADIUS_DEG) {
            return false;
        }

        // find the best edges
        List<StreetEdge> bestEdges = getBestEdges(candidateEdges, distances);

        for (StreetEdge edge : bestEdges) {
            link(vertex, edge, xscale);
        }

        // Warn if a linkage was made, but the linkage was suspiciously long.
        if (vertex instanceof TransitStop) {
            double distanceDegreesLatitude = distances.get(candidateEdges.get(0).getId());
            int distanceMeters = (int) SphericalDistanceLibrary.degreesLatitudeToMeters(distanceDegreesLatitude);
            if (distanceMeters > WARNING_DISTANCE_METERS) {
                // Registering an annotation but not logging because tests produce thousands of these warnings.
                graph.addBuilderAnnotation(new StopLinkedTooFar((TransitStop)vertex, distanceMeters));
            }
        }

        return true;
    }

    public void setAddExtraEdgesToAreas(Boolean addExtraEdgesToAreas) {
        this.addExtraEdgesToAreas = addExtraEdgesToAreas;
    }
}
