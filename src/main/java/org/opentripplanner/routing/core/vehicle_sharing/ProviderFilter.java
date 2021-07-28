package org.opentripplanner.routing.core.vehicle_sharing;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

public class ProviderFilter implements VehicleFilter {

    private static final int PROVIDER_INDEX = 0;

    private static final int VEHICLE_TYPE_INDEX = 1;

    private final Set<String> providers;

    private final Map<String, Set<VehicleType>> providersWithVehicleTypes;

    private final boolean allowed;

    private ProviderFilter(Set<String> providers, boolean allowed) {
        this.providers = providers.stream()
                .filter(p -> !p.contains("_"))
                .map(p -> p.toLowerCase(Locale.US))
                .collect(toSet());
        providersWithVehicleTypes = providers.stream()
                .filter(p -> p.contains("_"))
                .map(p -> p.split("_"))
                .peek(p -> p[PROVIDER_INDEX] = p[PROVIDER_INDEX].toLowerCase(Locale.US))
                .collect(groupingBy(p -> p[PROVIDER_INDEX]))
                .entrySet()
                .stream()
                .collect(toMap(Map.Entry::getKey, ProviderFilter::getVehicleTypesSet));
        this.allowed = allowed;
    }

    private static Set<VehicleType> getVehicleTypesSet(Map.Entry<String, List<String[]>> entry) {
        return entry.getValue().stream()
                .map(p -> p[VEHICLE_TYPE_INDEX])
                .map(VehicleType::fromString)
                .collect(toSet());
    }

    public static ProviderFilter providersAllowedFilter(Set<String> providersAllowed) {
        return new ProviderFilter(providersAllowed, true);
    }

    public static ProviderFilter providersDisallowedFilter(Set<String> providersDisallowed) {
        return new ProviderFilter(providersDisallowed, false);
    }

    @Override
    public boolean isValid(VehicleDescription vehicle) {
        return allowed == appliesTo(vehicle);
    }

    private boolean appliesTo(VehicleDescription vehicle) {
        String providerName = vehicle.getProvider().getProviderName().toLowerCase(Locale.US);
        return providers.contains(providerName)
                || Optional.ofNullable(providersWithVehicleTypes.get(providerName))
                .map(p -> p.contains(vehicle.getVehicleType()))
                .orElse(false);
    }
}
