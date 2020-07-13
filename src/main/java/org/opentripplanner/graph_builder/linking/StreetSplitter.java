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

    protected static final double RADIUS_DEG = SphericalDistanceLibrary.metersToDegrees(1000);

    protected final Graph graph;

    protected final HashGridSpatialIndex<Edge> idx;

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

    /**
     * Make the appropriate type of link edges from a vertex
     */
    protected abstract void makeLinkEdges(Vertex from, StreetVertex to);
}
