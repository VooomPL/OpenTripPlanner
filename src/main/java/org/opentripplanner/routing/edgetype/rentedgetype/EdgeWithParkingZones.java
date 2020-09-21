package org.opentripplanner.routing.edgetype.rentedgetype;

import org.opentripplanner.routing.core.vehicle_sharing.VehicleDescription;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Vertex;

import static java.util.Collections.emptyList;

public abstract class EdgeWithParkingZones extends Edge {

    private static final ParkingZoneInfo EMPTY_PARKING_ZONES = new ParkingZoneInfo(emptyList(), emptyList());

    private final ParkingZoneInfo parkingZones;

    protected EdgeWithParkingZones(Vertex v) {
        super(v, v);
        this.parkingZones = EMPTY_PARKING_ZONES;
    }

    public EdgeWithParkingZones(Vertex v, ParkingZoneInfo parkingZones) {
        super(v, v);
        this.parkingZones = parkingZones;
    }

    protected boolean canDropoffVehicleHere(VehicleDescription vehicle) {
        return parkingZones.canDropoffVehicleHere(vehicle);
    }
}
