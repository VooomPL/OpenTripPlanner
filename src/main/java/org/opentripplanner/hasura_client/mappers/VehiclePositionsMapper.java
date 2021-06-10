package org.opentripplanner.hasura_client.mappers;

import org.opentripplanner.hasura_client.HasuraGetter;
import org.opentripplanner.hasura_client.hasura_objects.Vehicle;
import org.opentripplanner.routing.core.vehicle_sharing.*;
import org.opentripplanner.updater.vehicle_sharing.vehicles_positions.SharedVehiclesSnapshotLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class VehiclePositionsMapper extends HasuraToOTPMapper<Vehicle, VehicleDescription> {

    private static final Logger LOG = LoggerFactory.getLogger(HasuraGetter.class);

    private Set<Provider> responsiveProviders = new HashSet<>();

    private static final VehiclePricingPackage DEFAULT_PRICING_PACKAGE = new VehiclePricingPackage();

    private final SharedVehiclesSnapshotLabel snapshotLabel;

    public VehiclePositionsMapper() {
        this.snapshotLabel = new SharedVehiclesSnapshotLabel();
    }

    public VehiclePositionsMapper(SharedVehiclesSnapshotLabel snapshotLabel) {
        this.snapshotLabel = snapshotLabel;
    }

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
        responsiveProviders.add(provider);
        VehicleDescription mappedVehicle;
        switch (vehicleType) {
            case CAR:
                mappedVehicle = new CarDescription(providerVehicleId, longitude, latitude, fuelType, gearbox, provider, rangeInMeters, pricingPackage);
                break;
            case MOTORBIKE:
                mappedVehicle = new MotorbikeDescription(providerVehicleId, longitude, latitude, fuelType, gearbox, provider, rangeInMeters, pricingPackage);
                break;
            case KICKSCOOTER:
                mappedVehicle = new KickScooterDescription(providerVehicleId, longitude, latitude, fuelType, gearbox, provider, rangeInMeters, pricingPackage);
                break;
            default:
                // this should never happen
                LOG.warn("Omitting vehicle {} because of unsupported type {}", providerVehicleId, vehicleType);
                return null;
        }
        mappedVehicle.setSnapshotLabel(snapshotLabel);

        return mappedVehicle;
    }

    public Set<Provider> getResponsiveProviders() {
        return responsiveProviders;
    }
}
