package org.opentripplanner.graph_builder.module.vehicle_sharing;

import org.junit.Before;
import org.junit.Test;
import org.opentripplanner.common.geometry.GeometryUtils;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.edgetype.StreetTraversalPermission;
import org.opentripplanner.routing.edgetype.rentedgetype.DropoffVehicleEdge;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.vertextype.IntersectionVertex;
import org.opentripplanner.routing.vertextype.StreetVertex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class VehicleSharingBuilderModuleTest {

    private VehicleSharingBuilderModule builderModuleWithoutParkingZones;

    private Graph graph;
    private StreetVertex v1, v2, v3;

    @Before
    public void setUp() {
        builderModuleWithoutParkingZones = VehicleSharingBuilderModule.withoutParkingZones();

        graph = new Graph();

        v1 = new IntersectionVertex(graph, "label1", 0, 0, "name1");
        v2 = new IntersectionVertex(graph, "label2", 1, 1, "name2");
        new StreetEdge(v1, v2, GeometryUtils.makeLineString(0, 0, 1, 1), "name", 2, StreetTraversalPermission.CAR, false);
        new StreetEdge(v2, v1, GeometryUtils.makeLineString(1, 1, 0, 0), "name", 2, StreetTraversalPermission.CAR, false);

        v3 = new IntersectionVertex(graph, "label3", 2, 2, "name3");
    }

    @Test
    public void shouldAddDropoffEdgesOnlyForVerticesConnectedToGraph() {
        // when
        builderModuleWithoutParkingZones.buildGraph(graph, null);

        // then
        assertEquals(2, v1.getOutgoing().size());
        assertEquals(2, v1.getIncoming().size());
        Edge dropoffEdge1 = v1.getOutgoing().stream().filter(DropoffVehicleEdge.class::isInstance).findFirst().get();
        assertTrue(v1.getIncoming().contains(dropoffEdge1));

        assertEquals(2, v2.getOutgoing().size());
        assertEquals(2, v2.getIncoming().size());
        Edge dropoffEdge2 = v2.getOutgoing().stream().filter(DropoffVehicleEdge.class::isInstance).findFirst().get();
        assertTrue(v2.getIncoming().contains(dropoffEdge2));

        assertTrue(v3.getOutgoing().isEmpty());
        assertTrue(v3.getIncoming().isEmpty());
    }
}
