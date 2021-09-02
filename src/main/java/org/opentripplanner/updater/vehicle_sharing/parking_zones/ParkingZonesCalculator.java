package org.opentripplanner.updater.vehicle_sharing.parking_zones;

import com.google.common.annotations.VisibleForTesting;
import org.locationtech.jts.geom.Point;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleType;
import org.opentripplanner.routing.edgetype.rentedgetype.ParkingZoneInfo;
import org.opentripplanner.routing.edgetype.rentedgetype.SingleParkingZone;
import org.opentripplanner.routing.graph.Vertex;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class ParkingZonesCalculator implements Serializable {

    private final List<GeometryParkingZone> geometryParkingZones;

    private final List<GeometriesDisallowedForVehicleType> geometriesDisallowedForVehicleTypes;

    private final List<GeometryParkingZone> mobilityHubParkingZones;

    @VisibleForTesting
    final List<SingleParkingZone> parkingZonesEnabled;

    public ParkingZonesCalculator(List<GeometryParkingZone> geometryParkingZones,
                                  List<GeometriesDisallowedForVehicleType> geometriesDisallowedForVehicleTypes,
                                  List<GeometryParkingZone> mobilityHubParkingZones) {
        this.geometryParkingZones = geometryParkingZones;
        this.geometriesDisallowedForVehicleTypes = geometriesDisallowedForVehicleTypes;
        this.mobilityHubParkingZones = mobilityHubParkingZones;
        this.parkingZonesEnabled = createParkingZonesEnabled();
    }

    public ParkingZonesCalculator(List<GeometryParkingZone> geometryParkingZones,
                                  List<GeometriesDisallowedForVehicleType> geometriesDisallowedForVehicleTypes) {
        this.geometryParkingZones = geometryParkingZones;
        this.geometriesDisallowedForVehicleTypes = geometriesDisallowedForVehicleTypes;
        this.mobilityHubParkingZones = emptyList();
        this.parkingZonesEnabled = createParkingZonesEnabled();
    }

    private List<SingleParkingZone> createParkingZonesEnabled() {
        return geometryParkingZones.stream()
                .map(gpz -> new SingleParkingZone(gpz.getProviderId(), gpz.getVehicleType()))
                .distinct()
                .collect(toList());
    }

    public ParkingZoneInfo getParkingZonesForLocation(Vertex vertex) {
        return getParkingZonesForLocation(vertex, null);
    }

    public ParkingZoneInfo getParkingZonesForLocation(Vertex vertex,
                                                      @Nullable VehicleType vehicleTypeExcludedFromCityParkingZones) {
        Point point = vertex.toPoint();
        List<SingleParkingZone> parkingZones = geometryParkingZones.stream()
                .map(gpz -> findMatchingParkingZone(point, gpz))
                .filter(Objects::nonNull)
                .collect(toList());
        List<VehicleType> vehicleTypesForbiddenFromParkingHere = getCityGovParkingZonesForLocation(point,
                vehicleTypeExcludedFromCityParkingZones);
        return new ParkingZoneInfo(parkingZones, parkingZonesEnabled, vehicleTypesForbiddenFromParkingHere);
    }

    private List<VehicleType> getCityGovParkingZonesForLocation(Point point, @Nullable VehicleType excluded) {
        return geometriesDisallowedForVehicleTypes.stream()
                .filter(geom -> geom.isPointInDisallowedParkingZone(point))
                .map(GeometriesDisallowedForVehicleType::getVehicleType)
                .filter(vehicleType -> vehicleType != excluded)
                .collect(toList());
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

    public boolean inMobilityHubZone(Vertex vertex) {
        return mobilityHubParkingZones.stream().anyMatch(gpz -> gpz.isPointInDisallowedParkingZone(vertex.toPoint()));
    }

    public ParkingZoneInfo getParkingZonesForLocationNearMobilityHub(Vertex vertex) {
        Point point = vertex.toPoint();
        List<SingleParkingZone> parkingZones = mobilityHubParkingZones.stream()
                .filter(gpz -> gpz.isPointInDisallowedParkingZone(vertex.toPoint()))
                .map(this::getMatchingParkingZoneFromList)
                .collect(toList());
        List<VehicleType> vehicleTypesForbiddenFromParkingHere = getCityGovParkingZonesForLocation(point,
                null);
        return new ParkingZoneInfo(parkingZones, parkingZonesEnabled, vehicleTypesForbiddenFromParkingHere);
    }
}
