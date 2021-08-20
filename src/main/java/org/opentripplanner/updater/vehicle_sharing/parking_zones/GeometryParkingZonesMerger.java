package org.opentripplanner.updater.vehicle_sharing.parking_zones;

import org.locationtech.jts.geom.Geometry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GeometryParkingZonesMerger {

    public List<GeometryParkingZone> merge(List<GeometryParkingZone> a, List<GeometryParkingZone> b) {
        List<GeometryParkingZone> result = new ArrayList<>();
        a.stream()
                .filter(geometryA -> b.stream().noneMatch(geometryA::sameProviderIdAndVehicleType))
                .forEach(result::add);
        b.stream()
                .filter(geometryB -> a.stream().noneMatch(geometryB::sameProviderIdAndVehicleType))
                .forEach(result::add);
        for (GeometryParkingZone geometryA : a) {
            for (GeometryParkingZone geometryB : b) {
                if (geometryA.sameProviderIdAndVehicleType(geometryB)) {
                    result.add(merge(geometryA, geometryB));
                    break;
                }
            }
        }
        return result;
    }

    private GeometryParkingZone merge(GeometryParkingZone a, GeometryParkingZone b) {
        List<Geometry> geometriesAllowed = Stream.of(a.getGeometriesAllowed(), b.getGeometriesAllowed())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        List<Geometry> geometriesDisallowed = Stream.of(a.getGeometriesDisallowed(), b.getGeometriesDisallowed())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        return new GeometryParkingZone(a.getProviderId(), a.getVehicleType(), geometriesAllowed, geometriesDisallowed);
    }
}
