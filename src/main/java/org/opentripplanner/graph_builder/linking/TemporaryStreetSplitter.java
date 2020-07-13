package org.opentripplanner.graph_builder.linking;

import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import jersey.repackaged.com.google.common.collect.Lists;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.index.SpatialIndex;
import org.locationtech.jts.linearref.LinearLocation;
import org.opentripplanner.common.geometry.HashGridSpatialIndex;
import org.opentripplanner.common.model.GenericLocation;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.edgetype.TemporaryFreeEdge;
import org.opentripplanner.routing.edgetype.rentedgetype.ParkingZoneInfo.SingleParkingZone;
import org.opentripplanner.routing.edgetype.rentedgetype.TemporaryDropoffVehicleEdge;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.location.TemporaryStreetLocation;
import org.opentripplanner.routing.vertextype.SplitterVertex;
import org.opentripplanner.routing.vertextype.StreetVertex;
import org.opentripplanner.routing.vertextype.TemporarySplitterVertex;
import org.opentripplanner.routing.vertextype.TransitStop;
import org.opentripplanner.util.LocalizedString;
import org.opentripplanner.util.NonLocalizedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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
public class TemporaryStreetSplitter extends StreetSplitter {

    private static final Logger LOG = LoggerFactory.getLogger(TemporaryStreetSplitter.class);

    private static final LocalizedString ORIGIN = new LocalizedString("origin", new String[]{});

    private static final LocalizedString DESTINATION = new LocalizedString("destination", new String[]{});

    private final SpatialIndex transitStopIndex;

    /**
     * Construct a new SimpleStreetSplitter.
     * NOTE: Only one SimpleStreetSplitter should be active on a graph at any given time.
     *
     * @param hashGridSpatialIndex If not null this index is used instead of creating new one
     * @param transitStopIndex     Index of all transitStops which is generated in {@link org.opentripplanner.routing.impl.StreetVertexIndexServiceImpl}
     */
    public TemporaryStreetSplitter(Graph graph, HashGridSpatialIndex<Edge> hashGridSpatialIndex,
                                   SpatialIndex transitStopIndex) {
        super(graph, hashGridSpatialIndex);
        this.transitStopIndex = transitStopIndex;
    }

    /**
     * Used to link origin and destination points to graph non destructively.
     * <p>
     * Split edges don't replace existing ones and only temporary edges and vertices are created.
     * <p>
     * Will throw TrivialPathException if origin and destination Location are on the same edge
     *
     * @param location
     * @param options
     * @param endVertex true if this is destination vertex
     * @return
     */
    public Vertex getClosestVertex(GenericLocation location, RoutingRequest options,
                                   boolean endVertex) {
        if (endVertex) {
            LOG.debug("Finding end vertex for {}", location);
        } else {
            LOG.debug("Finding start vertex for {}", location);
        }
        Coordinate coord = location.getCoordinate();
        String name;

        if (location.name == null || location.name.isEmpty()) {
            if (endVertex) {
                name = DESTINATION.toString(options.locale);
            } else {
                name = ORIGIN.toString(options.locale);
            }
        } else {
            name = location.name;
        }
        TemporaryStreetLocation closest = new TemporaryStreetLocation(UUID.randomUUID().toString(),
                coord, new NonLocalizedString(name), endVertex);

        TraverseMode nonTransitMode = TraverseMode.WALK;
        //It can be null in tests
        if (options != null) {
            TraverseModeSet modes = options.modes;
            if (modes.getCar())
                // for park and ride we will start in car mode and walk to the end vertex
                if (endVertex && options.parkAndRide) {
                    nonTransitMode = TraverseMode.WALK;
                } else {
                    nonTransitMode = TraverseMode.CAR;
                }
            else if (modes.getWalk())
                nonTransitMode = TraverseMode.WALK;
            else if (modes.getBicycle())
                nonTransitMode = TraverseMode.BICYCLE;
        }

        if (endVertex) {
            addTemporaryDropoffVehicleEdge(closest);
        }

        if (!link(closest, nonTransitMode, options)) {
            LOG.warn("Couldn't link {}", location);
        }
        return closest;
    }

    private void addTemporaryDropoffVehicleEdge(Vertex destination) {
        TemporaryDropoffVehicleEdge e = new TemporaryDropoffVehicleEdge(destination);
        if (graph.parkingZonesCalculator != null) {
            List<SingleParkingZone> parkingZonesEnabled = graph.parkingZonesCalculator.getNewParkingZonesEnabled();
            List<SingleParkingZone> parkingZones = graph.parkingZonesCalculator.getParkingZonesForRentEdge(e, parkingZonesEnabled);
            e.updateParkingZones(parkingZonesEnabled, parkingZones);
        }
    }

    /**
     * Make the appropriate type of link edges from a vertex
     */
    @Override
    protected void makeLinkEdges(Vertex from, StreetVertex to) {
        if (from instanceof TemporaryStreetLocation) {
            makeTemporaryEdges((TemporaryStreetLocation) from, to);
        } else {
            LOG.warn("Cannot create temporary edges for permanent vertex of type {}", from.getClass());
        }
    }

    /**
     * Make temporary edges to origin/destination vertex in origin/destination search
     **/
    private void makeTemporaryEdges(TemporaryStreetLocation from, Vertex to) {
        if (to instanceof TemporarySplitterVertex) {
            from.setWheelchairAccessible(((TemporarySplitterVertex) to).isWheelchairAccessible());
        }
        if (from.isEndVertex()) {
            LOG.debug("Linking end vertex to {} -> {}", to, from);
            new TemporaryFreeEdge(to, from);
        } else {
            LOG.debug("Linking start vertex to {} -> {}", from, to);
            new TemporaryFreeEdge(from, to);
        }
    }

    /**
     * split the edge and link in the transit stop
     */
    private void link(TemporaryStreetLocation tstop, StreetEdge edge, double xscale, RoutingRequest options) {
        // TODO: we've already built this line string, we should save it
        LineString orig = edge.getGeometry();
        LinearLocation ll = createLinearLocation(tstop, orig, xscale);
        if (tryLinkVertexToVertex(tstop, edge, orig, ll)) {
            return;
        }
        linkVertexOnEdge(tstop, edge, ll, options);
    }

    private void linkVertexOnEdge(TemporaryStreetLocation tstop, StreetEdge edge, LinearLocation ll, RoutingRequest options) {
        //This throws runtime TrivialPathException if same edge is split in origin and destination link
        //It is only used in origin/destination linking since otherwise options is null
        if (options != null) {
            options.canSplitEdge(edge);
        }
        // split the edge, get the split vertex
        SplitterVertex v0 = splitTemporarily(edge, ll, tstop.isEndVertex());
        makeTemporaryEdges(tstop, v0);
    }

    /**
     * Split the street edge at the given fraction
     *
     * @param edge           to be split
     * @param ll             fraction at which to split the edge
     * @param endVertex      if this is temporary edge this is true if this is end vertex otherwise it doesn't matter
     * @return Splitter vertex with added new edges
     */
    private SplitterVertex splitTemporarily(StreetEdge edge, LinearLocation ll, boolean endVertex) {
        LineString geometry = edge.getGeometry();

        // create the geometries
        Coordinate splitPoint = ll.getCoordinate(geometry);

        // every edge can be split exactly once, so this is a valid label
        SplitterVertex v = new TemporarySplitterVertex("split from " + edge.getId(), splitPoint.x, splitPoint.y, edge, endVertex);

        // Split the 'edge' at 'v' in 2 new edges and connect these 2 edges to the
        // existing vertices
        edge.split(v, false);
        return v;
    }

    private boolean tryLinkVertexToStop(Vertex vertex, Envelope env, double xscale) {
        // We only link to stops if we are searching for origin/destination and for that we need transitStopIndex.
        if (transitStopIndex == null) {
            return false;
        }
        LOG.debug("No street edge was found for {}", vertex);
        // We search for closest stops (since this is only used in origin/destination linking if no edges were found)
        // in the same way the closest edges are found.
        List<TransitStop> candidateStops = new ArrayList<>();
        transitStopIndex.query(env).forEach(candidateStop ->
                candidateStops.add((TransitStop) candidateStop)
        );

        final TIntDoubleMap stopDistances = new TIntDoubleHashMap();

        for (TransitStop t : candidateStops) {
            stopDistances.put(t.getIndex(), distance(vertex, t, xscale));
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
                makeTemporaryEdges((TemporaryStreetLocation) vertex, stop);
            }
            return true;
        }
    }

    /**
     * Link this vertex into the graph
     */
    protected boolean link(TemporaryStreetLocation vertex, TraverseMode traverseMode, RoutingRequest options) {
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
            return tryLinkVertexToStop(vertex, env, xscale);
        }

        // find the best edges
        List<StreetEdge> bestEdges = getBestEdges(candidateEdges, distances);

        for (StreetEdge edge : bestEdges) {
            link(vertex, edge, xscale, options);
        }
        return true;
    }
}
