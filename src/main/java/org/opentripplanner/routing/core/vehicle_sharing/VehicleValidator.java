package org.opentripplanner.routing.core.vehicle_sharing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Set.of;

public class VehicleValidator {

    private final List<VehicleFilter> filters = new ArrayList<>();

    public void addFilter(VehicleFilter filter) {
        filters.add(filter);
    }

    public boolean isValid(VehicleDescription vehicle) {
        return filters.stream().allMatch(f -> f.isValid(vehicle));
    }

    public Set<VehicleType> getVehicleTypesAllowed() {
        return filters.stream()
                .filter(VehicleTypeFilter.class::isInstance)
                .map(VehicleTypeFilter.class::cast)
                .map(VehicleTypeFilter::getVehicleTypes)
                .collect(() -> new HashSet<>(of(VehicleType.values())), Set::retainAll, Set::retainAll);
    }
}
