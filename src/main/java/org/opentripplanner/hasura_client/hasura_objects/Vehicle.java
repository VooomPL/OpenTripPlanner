package org.opentripplanner.hasura_client.hasura_objects;

import org.opentripplanner.updater.vehicle_sharing.vehicles_positions.VehicleProvider;

import java.math.BigDecimal;

public class Vehicle extends HasuraObject {

    private String providerVehicleId;
    private double longitude;
    private double latitude;
    private String fuelType;
    private String gearbox;
    private String type;
    private VehicleProvider provider;
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

    public VehicleProvider getProvider() {
        return provider;
    }

    public void setProvider(VehicleProvider provider) {
        this.provider = provider;
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
}
