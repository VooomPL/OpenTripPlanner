package org.opentripplanner.routing.edgetype.rentedgetype;

import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateEditor;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleDescription;
import org.opentripplanner.routing.edgetype.TemporaryEdge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.vertextype.TemporaryRentVehicleVertex;

import java.util.Locale;

/**
 * This edge allows us to rent vehicle (or leave current vehicle and rent new one).
 * This edge is a loop on {@link TemporaryRentVehicleVertex} which, when traversed, changes our current traverse mode,
 * but leaves us in the same location.
 */
public class RentVehicleEdge extends EdgeWithParkingZones implements TemporaryEdge {

    private final VehicleDescription vehicle;

    public RentVehicleEdge(TemporaryRentVehicleVertex v, VehicleDescription vehicle) {
        super(v);
        this.vehicle = vehicle;
    }

    public RentVehicleEdge(TemporaryRentVehicleVertex v, VehicleDescription vehicle, ParkingZoneInfo parkingZones) {
        super(v, parkingZones);
        this.vehicle = vehicle;
    }

    @Override
    public String getName() {
        return "Rent vehicle " + vehicle + " in node " + tov.getName();
    }

    @Override
    public String getName(Locale locale) {
        return "Rent vehicle " + vehicle + " in node " + tov.getName();
    }

    @Override
    public State traverse(State state) {
        if (!state.getOptions().rentingAllowed) {
            return null;
        }

        if (!state.getOptions().getAcceptedSharedVehiclesSnapshotLabel().equals(vehicle.getSnapshotLabel())) {
            return null;
        }

        //Do not use vehicle presence prediction when using historical vehicle positions
        if (vehicle.getSnapshotLabel().isEmpty() && !willVehicleBePresent(state)) {
            return null;
        }

        if (state.getOptions().vehicleValidator.isValid(vehicle)) {
            if (state.isCurrentlyRentingVehicle()) {
                return trySwitchVehicles(state);
            } else {
                return beginVehicleRenting(state);
            }
        }
        return null;
    }

    private boolean willVehicleBePresent(State state) {
        Graph graph = state.getContext().graph;

        if (graph.carPresencePredictor != null && state.getContext().opt.vehiclePredictionThreshold > 0) {
            double vehiclePresenceProbability = graph.carPresencePredictor.predict(vehicle, state.getTimeSeconds());
            return vehiclePresenceProbability >= state.getContext().opt.vehiclePredictionThreshold;
        }
        return true;
    }

    public State reversedTraverseSwitchVehicles(State state, VehicleDescription vehicle) {
        StateEditor next = state.edit(this);
        next.reversedBeginVehicleRenting();
        next.reversedDoneVehicleRenting(vehicle);
        return next.makeState();
    }

    public State reversedTraverseBeginRenting(State state) {
        StateEditor next = state.edit(this);
        next.reversedBeginVehicleRenting();
        return next.makeState();
    }

    protected State trySwitchVehicles(State state) {
        if (!canDropoffVehicleHere(state.getCurrentVehicle())) {
            return null;
        }
        StateEditor stateEditor = state.edit(this);
        stateEditor.doneVehicleRenting();
        stateEditor.beginVehicleRenting(vehicle);
        return stateEditor.makeState();
    }

    protected State beginVehicleRenting(State state) {
        StateEditor next = state.edit(this);
        next.beginVehicleRenting(vehicle);
        return next.makeState();
    }

    public VehicleDescription getVehicle() {
        return vehicle;
    }
}
