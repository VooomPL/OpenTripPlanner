package org.opentripplanner.updater.vehicle_sharing.vehicles_positions;

import org.opentripplanner.routing.core.vehicle_sharing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

public class VehiclePositionsMapper {

    private static final Logger LOG = LoggerFactory.getLogger(VehiclePositionsMapper.class);

    private Map<Provider, Long> numberOfMappedVehiclesPerProvider = new HashMap<>();

    public List<VehicleDescription> map(List<SharedVehiclesApiResponse.Vehicle> vehicles) {
        numberOfMappedVehiclesPerProvider.clear();
        return vehicles.stream()
                .map(this::mapToVehicleDescription)
                .filter(Objects::nonNull)
                .collect(toList());
    }

    private VehicleDescription mapToVehicleDescription(SharedVehiclesApiResponse.Vehicle vehicle) {
        if (vehicle.getProvider() == null) {
            LOG.warn("Omitting vehicle {} because of lack of provider", vehicle.getProviderVehicleId());
            return null;
        }
        if (!vehicle.getProvider().isAvailable()) {
            LOG.warn("Omitting vehicle {} because provider {} is unavailable", vehicle.getProviderVehicleId(), vehicle.getProvider().getName());
            return null;
        }
        String providerVehicleId = vehicle.getProviderVehicleId();
        double longitude = vehicle.getLongitude();
        double latitude = vehicle.getLatitude();
        FuelType fuelType = FuelType.fromString(vehicle.getFuelType());
        Gearbox gearbox = Gearbox.fromString(vehicle.getGearbox());
        Provider provider = new Provider(vehicle.getProvider().getId(), vehicle.getProvider().getName());
        Double rangeInMeters = vehicle.getRangeInMeters();
        VehicleType vehicleType = VehicleType.fromDatabaseVehicleType(vehicle.getType());
        if (vehicleType == null) {
            LOG.warn("Omitting vehicle {} because of unsupported type {}", providerVehicleId, vehicle.getType());
            return null;
        }
        VehicleDescription mappedVehicle = null;
        switch (vehicleType) {
            case CAR:
                mappedVehicle = new CarDescription(providerVehicleId, longitude, latitude, fuelType, gearbox, provider, rangeInMeters);
                break;
            case MOTORBIKE:
                mappedVehicle = new MotorbikeDescription(providerVehicleId, longitude, latitude, fuelType, gearbox, provider, rangeInMeters);
                break;
            case KICKSCOOTER:
                mappedVehicle = new KickScooterDescription(providerVehicleId, longitude, latitude, fuelType, gearbox, provider, rangeInMeters);
                break;
            default:
                // this should never happen
                LOG.warn("Omitting vehicle {} because of unsupported type {}", providerVehicleId, vehicleType);
        }
        numberOfMappedVehiclesPerProvider.merge(provider, 1L, Long::sum);
        return mappedVehicle;
    }

    public Map<Provider, Long> getNumberOfMappedVehiclesPerProvider() {
        return numberOfMappedVehiclesPerProvider;
    }
}
