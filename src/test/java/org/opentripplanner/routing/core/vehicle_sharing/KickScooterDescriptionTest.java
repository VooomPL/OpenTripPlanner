package org.opentripplanner.routing.core.vehicle_sharing;

import org.junit.Before;
import org.junit.Test;
import org.opentripplanner.common.geometry.GeometryUtils;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.edgetype.StreetTraversalPermission;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.vertextype.IntersectionVertex;

import static org.junit.Assert.assertEquals;

public class KickScooterDescriptionTest {

    private static final KickScooterDescription KICKSCOOTER = new KickScooterDescription("2", 0, 0, FuelType.ELECTRIC, Gearbox.AUTOMATIC, new Provider(1, "HopCity"));

    private IntersectionVertex v0, v1;

    @Before
    public void before() {
        Graph graph = new Graph();
        v0 = new IntersectionVertex(graph, "v0", 0.0, 0.0);
        v1 = new IntersectionVertex(graph, "v1", 2.0, 2.0);
    }

    @Test
    public void shouldReturnLowSpeedForPedestrianPath() {
        // given
        StreetEdge edge = new StreetEdge(v0, v1, GeometryUtils.makeLineString(0.002, 45, 0.003,
                45), "street", 100, StreetTraversalPermission.PEDESTRIAN, false);

        // then
        assertEquals(10. * (10. / 36.), KICKSCOOTER.getMaxSpeedInMetersPerSecond(edge), 0.001);
    }

    @Test
    public void shouldReturnMediumSpeedForRoad() {
        // given
        StreetEdge edge = new StreetEdge(v0, v1, GeometryUtils.makeLineString(0.002, 45, 0.003,
                45), "street", 100, StreetTraversalPermission.BICYCLE_AND_CAR, false);

        // then
        assertEquals(19. * (10. / 36.), KICKSCOOTER.getMaxSpeedInMetersPerSecond(edge), 0.001);
    }

    @Test
    public void shouldReturnHighSpeedForBikepath() {
        // given
        StreetEdge edge = new StreetEdge(v0, v1, GeometryUtils.makeLineString(0.002, 45, 0.003,
                45), "street", 100, StreetTraversalPermission.BICYCLE, false);

        // then
        assertEquals(20. * (10. / 36.), KICKSCOOTER.getMaxSpeedInMetersPerSecond(edge), 0.001);
    }
}