package org.opentripplanner.routing.edgetype.rentedgetype;

import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateEditor;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleDescription;
import org.opentripplanner.routing.graph.Vertex;

public class LowCostDropoffVehicleEdge extends DropoffVehicleEdge {

    public LowCostDropoffVehicleEdge(Vertex v) {
        super(v);
    }

    public LowCostDropoffVehicleEdge(Vertex v, ParkingZoneInfo parkingZones) {
        super(v, parkingZones);
    }

    @Override
    public State traverse(State state) {
        return state.getOptions().useMobilityHubs ? super.traverse(state) : null;
    }

    @Override
    public State reversedTraverseDoneRenting(State state, VehicleDescription vehicle) {
        StateEditor next = state.edit(this);
        next.reversedDoneVehicleRentingLowCost(vehicle);
        return next.makeState();
    }

    @Override
    protected State doneVehicleRenting(State state) {
        StateEditor stateEditor = state.edit(this);
        stateEditor.doneVehicleRentingLowCost();
        return stateEditor.makeState();
    }
}
