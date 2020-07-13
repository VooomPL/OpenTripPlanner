package org.opentripplanner.graph_builder.linking;

import com.google.common.collect.Iterables;
import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import jersey.repackaged.com.google.common.collect.Lists;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.linearref.LinearLocation;
import org.locationtech.jts.linearref.LocationIndexedLine;
import org.opentripplanner.common.geometry.GeometryUtils;
import org.opentripplanner.common.geometry.HashGridSpatialIndex;
import org.opentripplanner.common.geometry.SphericalDistanceLibrary;
import org.opentripplanner.graph_builder.services.DefaultStreetEdgeFactory;
import org.opentripplanner.graph_builder.services.StreetEdgeFactory;
import org.opentripplanner.openstreetmap.model.OSMWithTags;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.routing.edgetype.AreaEdge;
import org.opentripplanner.routing.edgetype.AreaEdgeList;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.edgetype.StreetTraversalPermission;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.vertextype.IntersectionVertex;
import org.opentripplanner.routing.vertextype.StreetVertex;
import org.opentripplanner.util.I18NString;
import org.opentripplanner.util.LocalizedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class StreetSplitter {

    private static final Logger LOG = LoggerFactory.getLogger(StreetSplitter.class);

    protected static final double RADIUS_DEG = SphericalDistanceLibrary.metersToDegrees(1000);

    /**
     * if there are two ways and the distances to them differ by less than this value, we link to both of them
     */
    protected static final double DUPLICATE_WAY_EPSILON_DEGREES = SphericalDistanceLibrary.metersToDegrees(0.001);

    protected final Graph graph;

    protected final HashGridSpatialIndex<Edge> idx;

    protected static final GeometryFactory geometryFactory = GeometryUtils.getGeometryFactory();

    /**
     * Construct a new SimpleStreetSplitter.
     * NOTE: Only one SimpleStreetSplitter should be active on a graph at any given time.
     *
     * @param hashGridSpatialIndex If not null this index is used instead of creating new one
     */
    protected StreetSplitter(Graph graph, HashGridSpatialIndex<Edge> hashGridSpatialIndex) {
        this.graph = graph;

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

    protected LinearLocation createLinearLocation(Vertex tstop, LineString orig, double xscale) {
        LineString transformed = equirectangularProject(orig, xscale);
        LocationIndexedLine il = new LocationIndexedLine(transformed);
        return il.project(new Coordinate(tstop.getLon() * xscale, tstop.getLat()));
    }

    protected boolean tryLinkVertexToVertex(Vertex tstop, StreetEdge edge, LineString orig, LinearLocation ll) {

        // if we're very close to one end of the line or the other, or endwise, don't bother to split,
        // cut to the chase and link directly
        // We use a really tiny epsilon here because we only want points that actually snap to exactly the same location on the
        // street to use the same vertices. Otherwise the order the stops are loaded in will affect where they are snapped.
        if (ll.getSegmentIndex() == 0 && ll.getSegmentFraction() < 1e-8) {
            makeLinkEdges(tstop, (StreetVertex) edge.getFromVertex());
            return true;
        }
        // -1 converts from count to index. Because of the fencepost problem, npoints - 1 is the "segment"
        // past the last point
        else if (ll.getSegmentIndex() == orig.getNumPoints() - 1) {
            makeLinkEdges(tstop, (StreetVertex) edge.getToVertex());
            return true;
        }

        // nPoints - 2: -1 to correct for index vs count, -1 to account for fencepost problem
        else if (ll.getSegmentIndex() == orig.getNumPoints() - 2 && ll.getSegmentFraction() > 1 - 1e-8) {
            makeLinkEdges(tstop, (StreetVertex) edge.getToVertex());
            return true;
        }
        return false;
    }


    protected List<StreetEdge> getCandidateEdges(Envelope env, TraverseMode traverseMode) {
        final TraverseModeSet traverseModeSet;
        if (traverseMode == TraverseMode.BICYCLE) {
            traverseModeSet = new TraverseModeSet(traverseMode, TraverseMode.WALK);
        } else {
            traverseModeSet = new TraverseModeSet(traverseMode);
        }
        // We sort the list of candidate edges by distance to the stop
        // This should remove any issues with things coming out of the spatial index in different orders
        // Then we link to everything that is within DUPLICATE_WAY_EPSILON_METERS of of the best distance
        // so that we capture back edges and duplicate ways.
        return idx.query(env).stream()
                .filter(streetEdge -> streetEdge instanceof StreetEdge)
                .map(edge -> (StreetEdge) edge)
                // note: not filtering by radius here as distance calculation is expensive
                // we do that below.
                .filter(edge -> edge.canTraverse(traverseModeSet) &&
                        // only link to edges still in the graph.
                        edge.getToVertex().getIncoming().contains(edge))
                .collect(Collectors.toList());
    }

    protected TIntDoubleMap getDistances(List<StreetEdge> candidateEdges, Vertex vertex, double xscale) {
        TIntDoubleMap distances = new TIntDoubleHashMap();
        candidateEdges.forEach(e -> distances.put(e.getId(), distance(vertex, e, xscale)));
        return distances;
    }

    protected void sortCandidateEdges(List<StreetEdge> candidateEdges, TIntDoubleMap distances) {
        // Sort the list.
        candidateEdges.sort((o1, o2) -> {
            double diff = distances.get(o1.getId()) - distances.get(o2.getId());
            // A Comparator must return an integer but our distances are doubles.
            if (diff < 0)
                return -1;
            if (diff > 0)
                return 1;
            return 0;
        });

    }

    protected List<StreetEdge> getBestEdges(List<StreetEdge> candidateEdges, TIntDoubleMap distances) {
        // find the best edges
        List<StreetEdge> bestEdges = Lists.newArrayList();

        // add edges until there is a break of epsilon meters.
        // we do this to enforce determinism. if there are a lot of edges that are all extremely close to each other,
        // we want to be sure that we deterministically link to the same ones every time. Any hard cutoff means things can
        // fall just inside or beyond the cutoff depending on floating-point operations.
        int i = 0;
        do {
            bestEdges.add(candidateEdges.get(i++));
        } while (i < candidateEdges.size() && distances.get(candidateEdges.get(i).getId()) - distances.get(candidateEdges.get(i - 1).getId()) < DUPLICATE_WAY_EPSILON_DEGREES);

        return bestEdges;
    }

    /**
     * Make the appropriate type of link edges from a vertex
     */
    protected abstract void makeLinkEdges(Vertex from, StreetVertex to);

    /**
     * projected distance from stop to edge, in latitude degrees
     */
    protected static double distance(Vertex tstop, StreetEdge edge, double xscale) {
        // Despite the fact that we want to use a fast somewhat inaccurate projection, still use JTS library tools
        // for the actual distance calculations.
        LineString transformed = equirectangularProject(edge.getGeometry(), xscale);
        return transformed.distance(geometryFactory.createPoint(new Coordinate(tstop.getLon() * xscale, tstop.getLat())));
    }

    /**
     * projected distance from stop to another stop, in latitude degrees
     */
    protected static double distance(Vertex tstop, Vertex tstop2, double xscale) {
        // use JTS internal tools wherever possible
        return new Coordinate(tstop.getLon() * xscale, tstop.getLat()).distance(new Coordinate(tstop2.getLon() * xscale, tstop2.getLat()));
    }

    /**
     * project this linestring to an equirectangular projection
     */
    private static LineString equirectangularProject(LineString geometry, double xscale) {
        Coordinate[] coords = new Coordinate[geometry.getNumPoints()];

        for (int i = 0; i < coords.length; i++) {
            Coordinate c = geometry.getCoordinateN(i);
            c = (Coordinate) c.clone();
            c.x *= xscale;
            coords[i] = c;
        }

        return geometryFactory.createLineString(coords);
    }
}
