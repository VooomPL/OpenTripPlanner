package org.opentripplanner.routing.edgetype.rentedgetype;

import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateEditor;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleDescription;
import org.opentripplanner.routing.edgetype.TemporaryEdge;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.vertextype.TemporaryRentVehicleVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class ShortcutEdge extends Edge implements TemporaryEdge {

    private static final Logger LOG = LoggerFactory.getLogger(ShortcutEdge.class);

    public final VehicleDescription vehicle, vehicle2;
    private final double distance;
    private final int duration;
    private final double weight;

    public ShortcutEdge(TemporaryRentVehicleVertex vertex1, TemporaryRentVehicleVertex vertex2, VehicleDescription vehicle, VehicleDescription vehicle2,
                        double distance, int duration, double weight) {
        super(vertex1, vertex2);

        this.vehicle = vehicle;
        this.vehicle2 = vehicle2;
        this.distance = distance;
        this.duration = duration;
        this.weight = weight;
    }


    @Override
    public State traverse(State s0) {
        if (!s0.getOptions().vehiclesNotComparable || !vehicle.equals(s0.getCurrentVehicle()) || !(s0.getBackEdge() instanceof RentVehicleEdge))
            return null;
        StateEditor next = s0.edit(this);
        next.incrementTimeInSeconds(duration);
        next.incrementWeight(weight);
        next.incrementWalkDistanceInMeters(distance);
        next.doneVehicleRenting();
        return next.makeState();
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getName(Locale locale) {
        return null;
    }
}
