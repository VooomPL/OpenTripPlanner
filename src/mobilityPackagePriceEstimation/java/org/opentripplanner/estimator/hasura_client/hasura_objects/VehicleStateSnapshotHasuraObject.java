package org.opentripplanner.estimator.hasura_client.hasura_objects;

import org.opentripplanner.hasura_client.hasura_objects.HasuraObject;
import org.opentripplanner.hasura_client.hasura_objects.Vehicle;
import org.opentripplanner.routing.core.vehicle_sharing.Provider;

import java.math.BigDecimal;

public class VehicleStateSnapshotHasuraObject extends HasuraObject {

    private String providerVehicleId;
    private double longitude;
    private double latitude;
    private String fuelType;
    private String gearbox;
    private String type;
    private Integer providerId;
    private Double range;
    private BigDecimal kmPrice;
    private BigDecimal drivingPrice;
    private BigDecimal startPrice;
    private BigDecimal stopPrice;
    private BigDecimal maxDailyPrice;

    public Double getRangeInMeters() {
        if (range != null)
            return range * 1000;
        else
            return null;
    }

    public String getProviderVehicleId() {
        return providerVehicleId;
    }

    public void setProviderVehicleId(String providerVehicleId) {
        this.providerVehicleId = providerVehicleId;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public String getGearbox() {
        return gearbox;
    }

    public void setGearbox(String gearbox) {
        this.gearbox = gearbox;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getProviderId() {
        return providerId;
    }

    public void setProviderId(Integer providerId) {
        this.providerId = providerId;
    }

    public void setRange(Double range) {
        this.range = range;
    }

    public Double getRange() {
        return range;
    }

    public BigDecimal getKmPrice() {
        return kmPrice;
    }

    public void setKmPrice(BigDecimal kmPrice) {
        this.kmPrice = kmPrice;
    }

    public BigDecimal getDrivingPrice() {
        return drivingPrice;
    }

    public void setDrivingPrice(BigDecimal drivingPrice) {
        this.drivingPrice = drivingPrice;
    }

    public BigDecimal getStartPrice() {
        return startPrice;
    }

    public void setStartPrice(BigDecimal startPrice) {
        this.startPrice = startPrice;
    }

    public BigDecimal getStopPrice() {
        return stopPrice;
    }

    public void setStopPrice(BigDecimal stopPrice) {
        this.stopPrice = stopPrice;
    }

    public BigDecimal getMaxDailyPrice() {
        return maxDailyPrice;
    }

    public void setMaxDailyPrice(BigDecimal maxDailyPrice) {
        this.maxDailyPrice = maxDailyPrice;
    }

    public Vehicle toVehicle(Provider provider) {
        Vehicle stateSnapshotAsVehicle = new Vehicle();
        stateSnapshotAsVehicle.setDrivingPrice(this.getDrivingPrice());
        stateSnapshotAsVehicle.setFuelType(this.getFuelType());
        stateSnapshotAsVehicle.setGearbox(this.getGearbox());
        stateSnapshotAsVehicle.setKmPrice(this.getKmPrice());
        stateSnapshotAsVehicle.setStopPrice(this.stopPrice);
        stateSnapshotAsVehicle.setLatitude(this.latitude);
        stateSnapshotAsVehicle.setLongitude(this.longitude);
        stateSnapshotAsVehicle.setMaxDailyPrice(this.maxDailyPrice);
        stateSnapshotAsVehicle.setProvider(provider.getProviderId() == this.providerId ? provider : null);
        stateSnapshotAsVehicle.setProviderVehicleId(this.providerVehicleId);
        stateSnapshotAsVehicle.setType(this.type);
        stateSnapshotAsVehicle.setStartPrice(this.startPrice);
        stateSnapshotAsVehicle.setRange(this.range);

        return stateSnapshotAsVehicle;
    }

}
