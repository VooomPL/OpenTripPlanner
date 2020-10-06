package org.opentripplanner.routing.edgetype.rentedgetype;

import org.opentripplanner.routing.core.vehicle_sharing.VehicleDescription;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Vertex;

import static java.util.Collections.emptyList;

public abstract class EdgeWithParkingZones extends Edge {

    private static final ParkingZoneInfo EMPTY_PARKING_ZONES = new ParkingZoneInfo(emptyList(), emptyList());
    private static final CityGovParkingZoneInfo EMPTY_CITY_PARKING_INFO = new CityGovParkingZoneInfo(emptyList());

    private final ParkingZoneInfo parkingZones;

    private final CityGovParkingZoneInfo cityGovParkingZoneInfo;

    protected EdgeWithParkingZones(Vertex v) {
        this(v, EMPTY_PARKING_ZONES);
    }

    protected EdgeWithParkingZones(Vertex v, ParkingZoneInfo parkingZones) {
        this(v, parkingZones, EMPTY_CITY_PARKING_INFO);
    }

    public EdgeWithParkingZones(Vertex v, ParkingZoneInfo parkingZones, CityGovParkingZoneInfo cityGovParkingZones) {
        super(v, v);
        this.parkingZones = parkingZones;
        this.cityGovParkingZoneInfo = cityGovParkingZones;
    }

    protected boolean canDropoffVehicleHere(VehicleDescription vehicle) {
        return cityGovParkingZoneInfo.doesCityGovernmentAllowParkingHere(vehicle)
                && parkingZones.doesProviderAllowParkingHere(vehicle);
    }
}
