package org.opentripplanner.hasura_client.mappers;

import org.opentripplanner.hasura_client.HasuraGetter;
import org.opentripplanner.hasura_client.hasura_objects.Vehicle;
import org.opentripplanner.routing.core.vehicle_sharing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Optional;

public class VehiclePositionsMapper extends HasuraToOTPMapper<Vehicle, VehicleDescription> {

    private static final Logger LOG = LoggerFactory.getLogger(HasuraGetter.class);

    private static final VehiclePricingPackage DEFAULT_PRICING_PACKAGE = new VehiclePricingPackage();

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

        BigDecimal startPrice, packagePrice, maxRentingPrice, kilometerPrice, drivingPriceInPackageExceeded, parkingPriceInPackageExceeded;
        int packageTimeLimitInSeconds;

        if (!provider.getProviderName().equals("Innogy")) {
            startPrice = Optional.ofNullable(vehicle.getStartPrice()).orElse(DEFAULT_PRICING_PACKAGE.getStartPrice());
            packagePrice = DEFAULT_PRICING_PACKAGE.getPackagePrice();
            packageTimeLimitInSeconds = DEFAULT_PRICING_PACKAGE.getPackageTimeLimitInSeconds();
            maxRentingPrice = Optional.ofNullable(vehicle.getMaxDailyPrice()).orElse(DEFAULT_PRICING_PACKAGE.getMaxRentingPrice());
        } else {
            //TODO: Remove this part, when correct pricing package data is available in the database
            startPrice = DEFAULT_PRICING_PACKAGE.getStartPrice();
            packagePrice = BigDecimal.valueOf(9.99);
            packageTimeLimitInSeconds = 480;
            maxRentingPrice = BigDecimal.valueOf(199);
        }
        drivingPriceInPackageExceeded = Optional.ofNullable(vehicle.getDrivingPrice()).orElse(DEFAULT_PRICING_PACKAGE.getDrivingPricePerTimeTickInPackageExceeded());
        kilometerPrice = Optional.ofNullable(vehicle.getKmPrice()).orElse(DEFAULT_PRICING_PACKAGE.getKilometerPrice());
        parkingPriceInPackageExceeded = Optional.ofNullable(vehicle.getStopPrice()).orElse(DEFAULT_PRICING_PACKAGE.getParkingPricePerTimeTickInPackageExceeded());

        VehiclePricingPackage pricingPackage = new VehiclePricingPackage(packagePrice, packageTimeLimitInSeconds,
                DEFAULT_PRICING_PACKAGE.getFreeSeconds(), DEFAULT_PRICING_PACKAGE.getMinRentingPrice(), startPrice,
                DEFAULT_PRICING_PACKAGE.getDrivingPricePerTimeTickInPackage(), DEFAULT_PRICING_PACKAGE.getParkingPricePerTimeTickInPackage(),
                drivingPriceInPackageExceeded, parkingPriceInPackageExceeded, kilometerPrice,
                DEFAULT_PRICING_PACKAGE.getSecondsPerTimeTickInPackage(), DEFAULT_PRICING_PACKAGE.getSecondsPerTimeTickInPackageExceeded(),
                maxRentingPrice, DEFAULT_PRICING_PACKAGE.isKilometerPriceEnabledAboveMaxRentingPrice());

        if (vehicleType == null) {
            LOG.warn("Omitting vehicle {} because of unsupported type {}", providerVehicleId, vehicle.getType());
            return null;
        }
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
}
