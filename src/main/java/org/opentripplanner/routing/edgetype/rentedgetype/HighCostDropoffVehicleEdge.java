package org.opentripplanner.routing.edgetype.rentedgetype;

import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateEditor;
import org.opentripplanner.routing.graph.Vertex;

public class HighCostDropoffVehicleEdge extends DropoffVehicleEdge {

    public HighCostDropoffVehicleEdge(Vertex v) {
        super(v);
    }

    public HighCostDropoffVehicleEdge(Vertex v, ParkingZoneInfo parkingZones) {
        super(v, parkingZones);
    }

    @Override
    public State traverse(State state) {
        if (!state.getOptions().useMobilityHubs) {
            return super.traverse(state);
        } else if (state.isCurrentlyRentingVehicle() && canDropoffVehicleHere(state.getCurrentVehicle())) {
            return doneVehicleRentingHighCost(state);
        } else {
            return null;
        }
    }

    protected State doneVehicleRentingHighCost(State state) {
        StateEditor stateEditor = state.edit(this);
        stateEditor.doneVehicleRentingHighCost();
        return stateEditor.makeState();
    }
}
