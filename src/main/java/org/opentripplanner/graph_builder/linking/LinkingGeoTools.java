package org.opentripplanner.graph_builder.linking;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.linearref.LinearLocation;
import org.locationtech.jts.linearref.LocationIndexedLine;
import org.opentripplanner.common.geometry.GeometryUtils;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.graph.Vertex;

public class LinkingGeoTools {

    private final GeometryFactory geometryFactory = GeometryUtils.getGeometryFactory();

    public LinearLocation createLinearLocation(Vertex tstop, LineString orig, double xscale) {
        LineString transformed = equirectangularProject(orig, xscale);
        LocationIndexedLine il = new LocationIndexedLine(transformed);
        return il.project(new Coordinate(tstop.getLon() * xscale, tstop.getLat()));
    }

    /**
     * projected distance from stop to another stop, in latitude degrees
     */
    public double distance(Vertex tstop, Vertex tstop2, double xscale) {
        // use JTS internal tools wherever possible
        return new Coordinate(tstop.getLon() * xscale, tstop.getLat()).distance(new Coordinate(tstop2.getLon() * xscale, tstop2.getLat()));
    }

    /**
     * projected distance from stop to edge, in latitude degrees
     */
    public double distance(Vertex tstop, StreetEdge edge, double xscale) {
        // Despite the fact that we want to use a fast somewhat inaccurate projection, still use JTS library tools
        // for the actual distance calculations.
        LineString transformed = equirectangularProject(edge.getGeometry(), xscale);
        return transformed.distance(geometryFactory.createPoint(new Coordinate(tstop.getLon() * xscale, tstop.getLat())));
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

    public LineString createLineString(Vertex from, Vertex to) {
        return geometryFactory.createLineString(new Coordinate[]{from.getCoordinate(), to.getCoordinate()});
    }
}
