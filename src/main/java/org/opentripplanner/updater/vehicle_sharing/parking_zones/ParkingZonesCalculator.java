package org.opentripplanner.updater.vehicle_sharing.parking_zones;

import com.google.common.annotations.VisibleForTesting;
import org.locationtech.jts.geom.Point;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleType;
import org.opentripplanner.routing.edgetype.rentedgetype.CityGovParkingZoneInfo;
import org.opentripplanner.routing.edgetype.rentedgetype.ParkingZoneInfo;
import org.opentripplanner.routing.edgetype.rentedgetype.SingleParkingZone;
import org.opentripplanner.routing.graph.Vertex;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

public class ParkingZonesCalculator implements Serializable {

    private final List<GeometryParkingZone> geometryParkingZones;

    private final List<GeometriesDisallowedForVehicleType> geometriesDisallowedForVehicleTypes;

    @VisibleForTesting
    final List<SingleParkingZone> parkingZonesEnabled;

    public ParkingZonesCalculator(List<GeometryParkingZone> geometryParkingZones) {
        this(geometryParkingZones, null);
    }

    public ParkingZonesCalculator(List<GeometryParkingZone> geometryParkingZones,
                                  List<GeometriesDisallowedForVehicleType> geometriesDisallowedForVehicleTypes) {
        this.geometryParkingZones = geometryParkingZones;
        this.geometriesDisallowedForVehicleTypes = geometriesDisallowedForVehicleTypes;
        this.parkingZonesEnabled = createParkingZonesEnabled();
    }

    private List<SingleParkingZone> createParkingZonesEnabled() {
        return geometryParkingZones.stream()
                .map(gpz -> new SingleParkingZone(gpz.getProviderId(), gpz.getVehicleType()))
                .distinct()
                .collect(toList());
    }

    // TODO AdamWiktor VMP-62 use when creating dropoff edges
    public CityGovParkingZoneInfo getCityGovParkingZonesForLocation(Vertex vertex) {
        Point point = vertex.toPoint();
        List<VehicleType> vehicleTypes = geometriesDisallowedForVehicleTypes.stream()
                .filter(geom -> geom.isPointInDisallowedParkingZone(point))
                .map(GeometriesDisallowedForVehicleType::getVehicleType)
                .collect(toList());
        return new CityGovParkingZoneInfo(vehicleTypes);
    }

    public ParkingZoneInfo getParkingZonesForLocation(Vertex vertex) {
        Point point = vertex.toPoint();
        List<SingleParkingZone> parkingZones = geometryParkingZones.stream()
                .map(gpz -> findMatchingParkingZone(point, gpz))
                .filter(Objects::nonNull)
                .collect(toList());
        return new ParkingZoneInfo(parkingZones, parkingZonesEnabled);
    }

    private SingleParkingZone findMatchingParkingZone(Point point, GeometryParkingZone geometryParkingZone) {
        if (geometryParkingZone.isPointInParkingZone(point)) {
            return getMatchingParkingZoneFromList(geometryParkingZone);
        } else {
            return null;
        }
    }

    private SingleParkingZone getMatchingParkingZoneFromList(GeometryParkingZone geometryParkingZone) {
        return parkingZonesEnabled.stream()
                .filter(pz -> pz.sameProviderIdAndVehicleType(geometryParkingZone))
                .findFirst()
                .orElse(null);
    }
}
