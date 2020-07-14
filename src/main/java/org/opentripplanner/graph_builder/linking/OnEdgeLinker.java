package org.opentripplanner.graph_builder.linking;

import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.linearref.LinearLocation;
import org.opentripplanner.common.geometry.SphericalDistanceLibrary;
import org.opentripplanner.graph_builder.services.StreetEdgeFactory;
import org.opentripplanner.openstreetmap.model.OSMWithTags;
import org.opentripplanner.routing.edgetype.AreaEdge;
import org.opentripplanner.routing.edgetype.AreaEdgeList;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.edgetype.StreetTraversalPermission;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.location.TemporaryStreetLocation;
import org.opentripplanner.routing.vertextype.IntersectionVertex;
import org.opentripplanner.routing.vertextype.SplitterVertex;
import org.opentripplanner.routing.vertextype.StreetVertex;
import org.opentripplanner.routing.vertextype.TransitStop;
import org.opentripplanner.util.I18NString;
import org.opentripplanner.util.LocalizedString;

import java.util.ArrayList;
import java.util.List;

public class OnEdgeLinker {

    private final StreetEdgeFactory edgeFactory;// = new DefaultStreetEdgeFactory(); // TODO in constructor

    private final Splitter splitter;

    private final EdgesMaker edgesMaker;

    private final LinkingGeoTools linkingGeoTools;

    private boolean addExtraEdgesToAreas = false;

    public OnEdgeLinker(StreetEdgeFactory edgeFactory, Splitter splitter, EdgesMaker edgesMaker,
                        LinkingGeoTools linkingGeoTools) {
        this.edgeFactory = edgeFactory;
        this.splitter = splitter;
        this.edgesMaker = edgesMaker;
        this.linkingGeoTools = linkingGeoTools;
    }

    public void setAddExtraEdgesToAreas(boolean addExtraEdgesToAreas) {
        this.addExtraEdgesToAreas = addExtraEdgesToAreas;
    }

    public void linkVertexOnEdgeTemporarily(TemporaryStreetLocation tstop, StreetEdge edge, LinearLocation ll) { // TODO rename variables
        // split the edge, get the split vertex
        SplitterVertex v0 = splitter.splitTemporarily(edge, ll, tstop.isEndVertex());
        edgesMaker.makeTemporaryEdges(tstop, v0);
    }

    public void linkVertexOnEdgePermanently(Vertex tstop, StreetEdge edge, LinearLocation ll) {
        // split the edge, get the split vertex
        SplitterVertex v0 = splitter.splitPermanently(edge, ll);
        edgesMaker.makePermanentEdges(tstop, v0);

        // If splitter vertex is part of area; link splittervertex to all other vertexes in area, this creates
        // edges that were missed by WalkableAreaBuilder
        if (edge instanceof AreaEdge && tstop instanceof TransitStop && this.addExtraEdgesToAreas) {
            linkTransitToAreaVertices(v0, ((AreaEdge) edge).getArea());
        }
    }

    // Link to all vertices in area/platform
    private void linkTransitToAreaVertices(Vertex splitterVertex, AreaEdgeList area) {
        List<Vertex> vertices = new ArrayList<>();

        for (AreaEdge areaEdge : area.getEdges()) {
            if (!vertices.contains(areaEdge.getToVertex())) vertices.add(areaEdge.getToVertex());
            if (!vertices.contains(areaEdge.getFromVertex())) vertices.add(areaEdge.getFromVertex());
        }

        for (Vertex vertex : vertices) {
            if (vertex instanceof StreetVertex && !vertex.equals(splitterVertex)) {
                LineString line = linkingGeoTools.createLineString(splitterVertex, vertex);
                double length = SphericalDistanceLibrary.distance(splitterVertex.getCoordinate(), vertex.getCoordinate());
                I18NString name = new LocalizedString("", new OSMWithTags());
                edgeFactory.createAreaEdge((IntersectionVertex) splitterVertex, (IntersectionVertex) vertex, line, name, length, StreetTraversalPermission.PEDESTRIAN_AND_BICYCLE, false, area);
                edgeFactory.createAreaEdge((IntersectionVertex) vertex, (IntersectionVertex) splitterVertex, line, name, length, StreetTraversalPermission.PEDESTRIAN_AND_BICYCLE, false, area);
            }
        }
    }
}
