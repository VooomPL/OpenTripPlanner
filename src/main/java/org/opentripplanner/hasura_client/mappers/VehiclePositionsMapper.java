package org.opentripplanner.hasura_client.mappers;

import org.opentripplanner.hasura_client.HasuraGetter;
import org.opentripplanner.hasura_client.hasura_objects.Vehicle;
import org.opentripplanner.routing.core.vehicle_sharing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class VehiclePositionsMapper extends HasuraToOTPMapper<Vehicle, VehicleDescription> {
    private static final Logger LOG = LoggerFactory.getLogger(HasuraGetter.class);

    private Map<Provider, Long> numberOfMappedVehiclesPerProvider = new HashMap<>();

    @Override
    protected VehicleDescription mapSingleHasuraObject(Vehicle vehicle) {
        if (vehicle.getProvider() == null) {
            LOG.warn("Omitting vehicle {} because of lack of provider", vehicle.getProviderVehicleId());
            return null;
        }
        String providerVehicleId = vehicle.getProviderVehicleId();
        double longitude = vehicle.getLongitude();
        double latitude = vehicle.getLatitude();
        FuelType fuelType = FuelType.fromString(vehicle.getFuelType());
        Gearbox gearbox = Gearbox.fromString(vehicle.getGearbox());
        Provider provider = new Provider(vehicle.getProvider().getProviderId(), vehicle.getProvider().getProviderName());
        Double rangeInMeters = vehicle.getRangeInMeters();
        VehicleType vehicleType = VehicleType.fromDatabaseVehicleType(vehicle.getType());
        VehiclePricingPackage pricingPackage = new VehiclePricingPackage();
        if (!provider.getProviderName().equals("Innogy")) {
            Optional.ofNullable(vehicle.getStartPrice()).ifPresent(pricingPackage::setStartPrice);
        } else {
            //TODO: Remove this part, when correct pricing package data is available in the database
            pricingPackage.setPackagePrice(BigDecimal.valueOf(9.99));
            pricingPackage.setPackageTimeLimitInSeconds(480);
            pricingPackage.setMaxRentingPrice(BigDecimal.valueOf(199));
        }
        Optional.ofNullable(vehicle.getMaxDailyPrice()).ifPresent(pricingPackage::setMaxRentingPrice);
        Optional.ofNullable(vehicle.getDrivingPrice()).ifPresent(pricingPackage::setDrivingPricePerTimeTickInPackageExceeded);
        Optional.ofNullable(vehicle.getKmPrice()).ifPresent(pricingPackage::setKilometerPrice);
        Optional.ofNullable(vehicle.getStopPrice()).ifPresent(pricingPackage::setParkingPricePerTimeTickInPackageExceeded);

        if (vehicleType == null) {
            LOG.warn("Omitting vehicle {} because of unsupported type {}", providerVehicleId, vehicle.getType());
            return null;
        }
        numberOfMappedVehiclesPerProvider.merge(provider, 1L, Long::sum);
        switch (vehicleType) {
            case CAR:
                return new CarDescription(providerVehicleId, longitude, latitude, fuelType, gearbox, provider, rangeInMeters, pricingPackage);
            case MOTORBIKE:
                return new MotorbikeDescription(providerVehicleId, longitude, latitude, fuelType, gearbox, provider, rangeInMeters, pricingPackage);
            case KICKSCOOTER:
                return new KickScooterDescription(providerVehicleId, longitude, latitude, fuelType, gearbox, provider, rangeInMeters, pricingPackage);
            default:
                // this should never happen
                LOG.warn("Omitting vehicle {} because of unsupported type {}", providerVehicleId, vehicleType);
                return null;
        }
    }

    public Map<Provider, Long> getNumberOfMappedVehiclesPerProvider() {
        return numberOfMappedVehiclesPerProvider;
    }
}
