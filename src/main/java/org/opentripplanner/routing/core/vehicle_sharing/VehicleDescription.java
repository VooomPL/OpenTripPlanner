package org.opentripplanner.routing.core.vehicle_sharing;


import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.updater.vehicle_sharing.vehicles_positions.SharedVehiclesSnapshotLabel;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Double.min;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "vehicleType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = KickScooterDescription.class, name = "KICKSCOOTER"),
        @JsonSubTypes.Type(value = CarDescription.class, name = "CAR"),
        @JsonSubTypes.Type(value = MotorbikeDescription.class, name = "MOTORBIKE"),
        @JsonSubTypes.Type(value = BikeDescription.class, name = "BIKE"),
})
@Getter
@EqualsAndHashCode(of = {"provider", "providerVehicleId", "snapshotLabel"})
public abstract class VehicleDescription {

    private final String providerVehicleId;
    private final double longitude;
    private final double latitude;
    private final double rangeInMeters;

    @Setter
    @JsonIgnore
    private SharedVehiclesSnapshotLabel snapshotLabel = new SharedVehiclesSnapshotLabel();

    @JsonSerialize
    private final List<VehiclePricingPackage> vehiclePricingPackages;

    protected boolean requiresHubToDrop;

    @JsonSerialize
    private final FuelType fuelType;

    @JsonSerialize
    private final Gearbox gearbox;

    @JsonUnwrapped
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private final Provider provider;

    public VehicleDescription(String providerVehicleId, double longitude, double latitude, FuelType fuelType,
                              Gearbox gearbox, Provider provider, boolean requiresHubToDrop) {
        this(providerVehicleId, longitude, latitude, fuelType, gearbox, provider, null, requiresHubToDrop, new VehiclePricingPackage());
    }

    public VehicleDescription(String providerVehicleId, double longitude, double latitude, FuelType fuelType,
                              Gearbox gearbox, Provider provider) {
        this(providerVehicleId, longitude, latitude, fuelType, gearbox, provider, null, false, new VehiclePricingPackage());
    }

    public VehicleDescription(String providerVehicleId, double longitude, double latitude, FuelType fuelType,
                              Gearbox gearbox, Provider provider, Double rangeInMeters, VehiclePricingPackage pricingPackage) {
        this(providerVehicleId, longitude, latitude, fuelType, gearbox, provider, rangeInMeters, false, pricingPackage);
    }

    public VehicleDescription(String providerVehicleId, double longitude, double latitude, FuelType fuelType,
                              Gearbox gearbox, Provider provider, Double rangeInMeters) {
        this(providerVehicleId, longitude, latitude, fuelType, gearbox, provider, rangeInMeters, false, new VehiclePricingPackage());

    }

    public VehicleDescription(String providerVehicleId, double longitude, double latitude, FuelType fuelType,
                              Gearbox gearbox, Provider provider, Double rangeInMeters, boolean requiresHubToDrop, VehiclePricingPackage vehiclePricingPackage) {
        if (rangeInMeters == null)
            rangeInMeters = this.getDefaultRangeInMeters();

        rangeInMeters = min(rangeInMeters, getMaximumRangeInMeters());

        this.providerVehicleId = providerVehicleId;
        this.longitude = longitude;
        this.latitude = latitude;
        this.fuelType = fuelType;
        this.gearbox = gearbox;
        this.provider = provider;
        this.rangeInMeters = rangeInMeters;
        this.vehiclePricingPackages = new ArrayList<>();
        this.vehiclePricingPackages.add(vehiclePricingPackage);
        this.requiresHubToDrop = requiresHubToDrop;
    }

    @Override
    public String toString() {
        return "VehicleDescription{" +
                "providerVehicleId=" + providerVehicleId +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", fuelType=" + fuelType +
                ", gearbox=" + gearbox +
                ", providerId=" + provider.getProviderId() +
                ", providerName=" + provider.getProviderName() +
                '}';
    }

    public double getRangeInMeters() {
        return rangeInMeters;
    }

    /**
     * Returns maximum speed on given street. Trivial getter for most vehicles.
     */
    @JsonIgnore
    public abstract double getMaxSpeedInMetersPerSecond(StreetEdge streetEdge);

    @JsonIgnore
    public abstract TraverseMode getTraverseMode();

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public abstract VehicleType getVehicleType();

    protected abstract double getDefaultRangeInMeters();

    protected Double getMaximumRangeInMeters() {
        return Double.MAX_VALUE;
    }

    @JsonIgnore
    public VehiclePricingPackage getVehiclePricingPackage(int index) {
        return vehiclePricingPackages.get(index);
    }

    public List<VehiclePricingPackage> getVehiclePricingPackages() {
        return vehiclePricingPackages;
    }

    public boolean requiresHubToDrop() {
        return requiresHubToDrop;
    }
}
