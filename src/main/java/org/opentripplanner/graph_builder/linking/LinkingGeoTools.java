package org.opentripplanner.graph_builder.linking;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.linearref.LinearLocation;
import org.locationtech.jts.linearref.LocationIndexedLine;
import org.opentripplanner.common.geometry.GeometryUtils;
import org.opentripplanner.common.geometry.HashGridSpatialIndex;
import org.opentripplanner.common.geometry.SphericalDistanceLibrary;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;

/**
 * Holds all functions useful in vertex linking, that are manipulating with geometry and locations calculating
 */
public class LinkingGeoTools {

    protected static final double RADIUS_DEG = SphericalDistanceLibrary.metersToDegrees(1000);

    private static final GeometryFactory geometryFactory = GeometryUtils.getGeometryFactory();

    /**
     * creates new spatial index of street edges in graph
     */
    public static HashGridSpatialIndex<Edge> createHashGridSpatialIndex(Graph graph) {
        HashGridSpatialIndex<Edge> index = new HashGridSpatialIndex<>();
        graph.getEdges().stream()
                .filter((StreetEdge.class)::isInstance)
                .map(StreetEdge.class::cast)
                .forEach(se -> index.insert(se.getGeometry(), se));
        return index;
    }

    /**
     * finds closest point to given vertex in a sequence of points (LineString)
     */
    public LinearLocation findLocationClosestToVertex(Vertex vertex, LineString lineString) {
        double xscale = createXScale(vertex);
        LineString transformed = equirectangularProject(lineString, xscale);
        LocationIndexedLine il = new LocationIndexedLine(transformed);
        return il.project(new Coordinate(vertex.getLon() * xscale, vertex.getLat()));
    }

    /**
     * projected distance from vertex to another vertex, in latitude degrees
     */
    public double distance(Vertex from, Vertex to) {
        double xscale = createXScale(from);
        // use JTS internal tools wherever possible
        return new Coordinate(from.getLon() * xscale, from.getLat()).distance(new Coordinate(to.getLon() * xscale, to.getLat()));
    }

    /**
     * projected distance from vertex to edge, in latitude degrees
     */
    public double distance(Vertex vertex, Edge edge) {
        double xscale = createXScale(vertex);
        // Despite the fact that we want to use a fast somewhat inaccurate projection, still use JTS library tools
        // for the actual distance calculations.
        LineString transformed = equirectangularProject(edge.getGeometry(), xscale);
        return transformed.distance(geometryFactory.createPoint(new Coordinate(vertex.getLon() * xscale, vertex.getLat())));
    }

    /**
     * create straight line between vertexes
     */
    public LineString createLineString(Vertex from, Vertex to) {
        return geometryFactory.createLineString(new Coordinate[]{from.getCoordinate(), to.getCoordinate()});
    }

    /**
     * wrap vertex coords in an envelope for searching in spatial index {@link HashGridSpatialIndex}
     */
    public Envelope createEnvelope(Vertex vertex) {
        // find nearby street edges
        // TODO: we used to use an expanding-envelope search, which is more efficient in
        // dense areas. but first let's see how inefficient this is. I suspect it's not too
        // bad and the gains in simplicity are considerable.
        Envelope env = new Envelope(vertex.getCoordinate());
        // Expand more in the longitude direction than the latitude direction to account for converging meridians.
        env.expandBy(RADIUS_DEG / createXScale(vertex), RADIUS_DEG);
        return env;
    }

    /**
     * project this linestring to an equirectangular projection
     */
    private LineString equirectangularProject(LineString geometry, double xscale) {
        Coordinate[] coords = new Coordinate[geometry.getNumPoints()];
        for (int i = 0; i < coords.length; i++) {
            Coordinate c = geometry.getCoordinateN(i);
            c = (Coordinate) c.clone();
            c.x *= xscale;
            coords[i] = c;
        }
        return geometryFactory.createLineString(coords);
    }

    /**
     * performs a simple local equirectangular projection, so distances are expressed in degrees latitude
     */
    private double createXScale(Vertex vertex) {
        return Math.cos(vertex.getLat() * Math.PI / 180);
    }
}
