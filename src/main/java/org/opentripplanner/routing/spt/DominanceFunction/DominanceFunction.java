package org.opentripplanner.routing.spt.DominanceFunction;

import org.opentripplanner.common.geometry.SphericalDistanceLibrary;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.edgetype.SimpleTransfer;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.edgetype.TimedTransferEdge;
import org.opentripplanner.routing.edgetype.rentedgetype.DropoffVehicleEdge;
import org.opentripplanner.routing.spt.ShortestPathTree;

import java.io.Serializable;
import java.util.Objects;

/**
 * A class that determines when one search branch prunes another at the same Vertex, and ultimately which solutions
 * are retained. In the general case, one branch does not necessarily win out over the other, i.e. multiple states can
 * coexist at a single Vertex.
 * <p>
 * Even functions where one state always wins (least weight, fastest travel time) are applied within a multi-state
 * shortest path tree because bike rental, car or bike parking, and turn restrictions all require multiple incomparable
 * states at the same vertex. These need the graph to be "replicated" into separate layers, which is achieved by
 * applying the main dominance logic (lowest weight, lowest cost, Pareto) conditionally, only when the two states
 * have identical bike/car/turn direction status.
 * <p>
 * Dominance functions are serializable so that routing requests may passed between machines in different JVMs, for instance
 * in OTPA Cluster.
 */
public abstract class DominanceFunction implements Serializable {
    protected DominanceFunctionSettings settings = new DominanceFunctionSettings();

    public void setSettings(DominanceFunctionSettings settings) {
        this.settings = settings;
    }

    private static final long serialVersionUID = 1;

    /**
     * Return true if the first state "defeats" the second state or at least ties with it in terms of suitability.
     * In the case that they are tied, we still want to return true so that an existing state will kick out a new one.
     * Provide this custom logic in subclasses. You would think this could be static, but in Java for some reason
     * calling a static function will call the one on the declared type, not the runtime instance type.
     */
    protected abstract boolean betterOrEqual(State a, State b);

    /**
     * For bike rental, parking, and approaching turn-restricted intersections states are incomparable:
     * they exist on separate planes. The core state dominance logic is wrapped in this public function and only
     * applied when the two states have all these variables in common (are on the same plane).
     */
    public boolean betterOrEqualAndComparable(State a, State b) {

        // States before boarding transit and after riding transit are incomparable.
        // This allows returning transit options even when walking to the destination is the optimal strategy.
        if (a.isEverBoarded() != b.isEverBoarded()) {
            return false;
        }

        if (a.getNonTransitMode() != b.getNonTransitMode())
            return false;

        // The result of a SimpleTransfer must not block alighting normally from transit. States that are results of
        // SimpleTransfers are incomparable with states that are not the result of SimpleTransfers.
        if ((a.backEdge instanceof SimpleTransfer) != (b.backEdge instanceof SimpleTransfer)) {
            return false;
        }

        // A TimedTransferEdge might be invalidated later, when we have boarded the next trip and have all the information
        // we need to check the specificity. We do not want states that might be invalidated to dominate other valid
        // states.
        if ((a.backEdge instanceof TimedTransferEdge) || (b.backEdge instanceof TimedTransferEdge)) {
            return false;
        }

        // Does one state represent riding a rented bike and the other represent walking before/after rental?
        if (a.isBikeRenting() != b.isBikeRenting()) {
            return false;
        }

        // In case of bike renting, different networks (ie incompatible bikes) are not comparable
        if (a.isBikeRenting()) {
            if (!Objects.equals(a.getBikeRentalNetworks(), b.getBikeRentalNetworks()))
                return false;
        }

        // Does one state represent driving a car and the other represent walking after the car was parked?
        if (a.isCarParked() != b.isCarParked()) {
            return false;
        }

        // Does one state represent riding a bike and the other represent walking after the bike was parked?
        if (a.isBikeParked() != b.isBikeParked()) {
            return false;
        }

        if (a.getCurrentVehicle() != null && b.getCurrentVehicle() != null) {
            if (settings.isDifferProvider() && a.getCurrentVehicle().getProvider() != b.getCurrentVehicle().getProvider()) {
                return false;
            }
            if (a.getCurrentVehicle().getVehicleType() != b.getCurrentVehicle().getVehicleType()) {
                return false;
            }
            if (settings.isDifferEnoughRange()) {
                double remainingDistanceFromA = SphericalDistanceLibrary
                        .fastDistance(a.getVertex().getLat(), a.getVertex().getLon(),
                                a.getOptions().to.getCoordinate().x, a.getOptions().to.getCoordinate().y);

                double remainingDistanceFromB = SphericalDistanceLibrary
                        .fastDistance(b.getVertex().getLat(), b.getVertex().getLon(),
                                b.getOptions().to.getCoordinate().x, b.getOptions().to.getCoordinate().y);

                double remainingRangeA = a.getCurrentVehicle().getRangeInMeters() - a.getDistanceTraversedInCurrentVehicle();
                double remainingRangeB = b.getCurrentVehicle().getRangeInMeters() - b.getDistanceTraversedInCurrentVehicle();

                if (((remainingDistanceFromA * 1.5 - remainingRangeA) > 0) != ((remainingDistanceFromB * 1.5 - remainingRangeB) > 0)) {
                    return false;
                }

            }
            if (settings.isDifferRange()) {
                double remainingRangeA = a.getCurrentVehicle().getRangeInMeters() - a.getDistanceTraversedInCurrentVehicle();
                double remainingRangeB = b.getCurrentVehicle().getRangeInMeters() - b.getDistanceTraversedInCurrentVehicle();

                if (remainingRangeA > remainingRangeB * 3 || remainingRangeB > remainingRangeB * 3) {
                    return false;
                }
            }
            if (settings.isDifferMayLeaveAtDestination()) {
                boolean canDroppA = a.getOptions().getRoutingContext().toVertex
                        .getOutgoingStreetEdges()
                        .stream()
                        .filter(DropoffVehicleEdge.class::isInstance)
                        .anyMatch(e -> e.traverse(a) != null);

                boolean canDroppB = b.getOptions().getRoutingContext().toVertex
                        .getOutgoingStreetEdges()
                        .stream()
                        .filter(DropoffVehicleEdge.class::isInstance)
                        .anyMatch(e -> e.traverse(b) != null);

                if (canDroppA != canDroppB) {
                    System.out.println("\n\nDifferent destination zones\n\n");
                    return false;
                }
            }
        }

        // Are the two states arriving at a vertex from two different directions where turn restrictions apply?
        if (a.backEdge != b.getBackEdge() && (a.backEdge instanceof StreetEdge)) {
            if (!((StreetEdge) a.backEdge).getTurnRestrictions().isEmpty())
                return false;
        }

        // These two states are comparable (they are on the same "plane" or "copy" of the graph).
        return betterOrEqual(a, b);

    }

    /**
     * Create a new shortest path tree using this function, considering whether it allows co-dominant States.
     * MultiShortestPathTree is the general case -- it will work with both single- and multi-state functions.
     */
    public ShortestPathTree getNewShortestPathTree(RoutingRequest routingRequest) {
        return new ShortestPathTree(routingRequest, this);
    }

}
