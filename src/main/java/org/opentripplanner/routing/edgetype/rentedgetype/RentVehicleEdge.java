package org.opentripplanner.routing.edgetype.rentedgetype;

import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateEditor;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleDescription;
import org.opentripplanner.routing.edgetype.TemporaryEdge;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Vertex;

import java.util.List;
import java.util.Locale;

public class RentVehicleEdge extends Edge implements TemporaryEdge {

    private final VehicleDescription vehicle;

    private final ParkingZoneInfo parkingZones;

    private final ParkingZoneInfo parkingZonesEnabled;

    public RentVehicleEdge(Vertex v, VehicleDescription vehicle, ParkingZoneInfo parkingZones,
                           ParkingZoneInfo parkingZonesEnabled) {
        super(v, v);
        this.vehicle = vehicle;
        this.parkingZones = parkingZones;
        this.parkingZonesEnabled = parkingZonesEnabled;
    }

    @Override
    public String getName() {
        return "Rent vehicle " + vehicle;
    }

    @Override
    public String getName(Locale locale) {
        return "Rent vehicle " + vehicle;
    }

    @Override
    public State traverse(State s0) {
        if (!s0.getOptions().rentingAllowed) {
            return null;
        }
        if (s0.getOptions().vehicleValidator.isValid(vehicle)) {
            if (s0.isCurrentlyRentingVehicle()) {
                return trySwitchVehicles(s0, vehicle);
            } else {
                return beginVehicleRenting(s0, vehicle);
            }
        }
        return null;
    }

//
//    public State reversedTraverseDoneRenting(State s0, VehicleDescription vehicle) { TODO
//        StateEditor next = s0.edit(this);
//        next.reversedDoneVehicleRenting(vehicle);
//        return next.makeState();
//    }
//
//    public State reversedTraverseBeginRenting(State s0) {
//        StateEditor next = s0.edit(this);
//        next.reversedBeginVehicleRenting();
//        return next.makeState();
//    }

    private State trySwitchVehicles(State s0, VehicleDescription vehicle) {
        if (!canDropoffVehicleHere(s0.getCurrentVehicle())) {
            return null;
        }
        StateEditor stateEditor = s0.edit(this);
        stateEditor.doneVehicleRenting();
        stateEditor.beginVehicleRenting(vehicle);
        return stateEditor.makeState();
    }

    private boolean canDropoffVehicleHere(VehicleDescription vehicle) {
        return !parkingZonesEnabled.appliesToVehicle(vehicle) || parkingZones.appliesToVehicle(vehicle);
    }

    private State beginVehicleRenting(State s0, VehicleDescription vehicle) {
        StateEditor next = s0.edit(this);
        next.beginVehicleRenting(vehicle);
        return next.makeState();
    }

    public VehicleDescription getVehicle() {
        return vehicle;
    }
}
