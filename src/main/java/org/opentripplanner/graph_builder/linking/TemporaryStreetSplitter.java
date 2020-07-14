package org.opentripplanner.graph_builder.linking;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.index.SpatialIndex;
import org.opentripplanner.common.geometry.HashGridSpatialIndex;
import org.opentripplanner.common.model.GenericLocation;
import org.opentripplanner.graph_builder.services.DefaultStreetEdgeFactory;
import org.opentripplanner.graph_builder.services.StreetEdgeFactory;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.routing.edgetype.rentedgetype.ParkingZoneInfo.SingleParkingZone;
import org.opentripplanner.routing.edgetype.rentedgetype.TemporaryDropoffVehicleEdge;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.location.TemporaryStreetLocation;
import org.opentripplanner.routing.vertextype.StreetVertex;
import org.opentripplanner.util.LocalizedString;
import org.opentripplanner.util.NonLocalizedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class TemporaryStreetSplitter {

    private static final Logger LOG = LoggerFactory.getLogger(TemporaryStreetSplitter.class);

    private static final LocalizedString ORIGIN = new LocalizedString("origin", new String[]{});

    private static final LocalizedString DESTINATION = new LocalizedString("destination", new String[]{});

    private final Graph graph;

    private final Linker linker;

    private final ToTransitStopLinker toTransitStopLinker;

    /**
     * Construct a new SimpleStreetSplitter.
     * NOTE: Only one SimpleStreetSplitter should be active on a graph at any given time.
     *
     * @param hashGridSpatialIndex If not null this index is used instead of creating new one
     * @param transitStopIndex     Index of all transitStops which is generated in {@link org.opentripplanner.routing.impl.StreetVertexIndexServiceImpl}
     */
    public TemporaryStreetSplitter(Graph graph, Linker linker, ToTransitStopLinker toTransitStopLinker) {
        this.graph = graph;
        this.linker = linker;
        this.toTransitStopLinker = toTransitStopLinker;
    }

    public static TemporaryStreetSplitter createNewDefaultInstance(Graph graph, HashGridSpatialIndex<Edge> index, SpatialIndex transitStopIndex) {
        if (index == null) {
            index = LinkingGeoTools.createHashGridSpatialIndex(graph);
        }
        StreetEdgeFactory streetEdgeFactory = new DefaultStreetEdgeFactory();
        EdgesMaker edgesMaker = new EdgesMaker();
        LinkingGeoTools linkingGeoTools = new LinkingGeoTools();
        Splitter splitter = new Splitter(graph, index);
        CandidateEdgesProvider candidateEdgesProvider = new CandidateEdgesProvider(index, linkingGeoTools);
        OnEdgeLinker onEdgeLinker = new OnEdgeLinker(streetEdgeFactory, splitter, edgesMaker, linkingGeoTools);
        Linker linker = new Linker(onEdgeLinker, candidateEdgesProvider, linkingGeoTools, edgesMaker);
        ToTransitStopLinker toTransitStopLinker = new ToTransitStopLinker(transitStopIndex, linkingGeoTools, edgesMaker);
        return new TemporaryStreetSplitter(graph, linker, toTransitStopLinker);
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
        TemporaryStreetLocation closest = createTemporaryVertex(location, options, endVertex);
        TraverseMode nonTransitMode = createTraverseMode(options, endVertex);
        if (endVertex) {
            addTemporaryDropoffVehicleEdge(closest);
        }
        if (!linker.linkTemporarily(closest, nonTransitMode, options)) {
            if (!toTransitStopLinker.tryLinkVertexToStop(closest)) {
                LOG.warn("Couldn't link {}", location);
            }
        }
        return closest;
    }

    private TemporaryStreetLocation createTemporaryVertex(GenericLocation location, RoutingRequest options,
                                                          boolean endVertex) {
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
        return new TemporaryStreetLocation(UUID.randomUUID().toString(), coord, new NonLocalizedString(name), endVertex);
    }

    private TraverseMode createTraverseMode(RoutingRequest options, boolean endVertex) {
        //It can be null in tests
        if (options != null) {
            TraverseModeSet modes = options.modes;
            if (modes.getCar())
                // for park and ride we will start in car mode and walk to the end vertex
                if (endVertex && options.parkAndRide) {
                    return TraverseMode.WALK;
                } else {
                    return TraverseMode.CAR;
                }
            else if (modes.getWalk())
                return TraverseMode.WALK;
            else if (modes.getBicycle())
                return TraverseMode.BICYCLE;
        }
        return TraverseMode.WALK;
    }

    private void addTemporaryDropoffVehicleEdge(Vertex destination) {
        TemporaryDropoffVehicleEdge e = new TemporaryDropoffVehicleEdge(destination);
        if (graph.parkingZonesCalculator != null) {
            List<SingleParkingZone> parkingZonesEnabled = graph.parkingZonesCalculator.getNewParkingZonesEnabled();
            List<SingleParkingZone> parkingZones = graph.parkingZonesCalculator.getParkingZonesForRentEdge(e, parkingZonesEnabled);
            e.updateParkingZones(parkingZonesEnabled, parkingZones);
        }
    }
}
