package org.opentripplanner.hasura_client.hasura_objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.opentripplanner.routing.core.vehicle_sharing.Provider;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class Vehicle extends HasuraObject {

    private String providerVehicleId;
    private double longitude;
    private double latitude;
    private String fuelType;
    private String gearbox;
    private String type;
    private Provider provider;
    private Double range;
    private BigDecimal kmPrice;
    private BigDecimal drivingPrice;
    private BigDecimal startPrice;
    private BigDecimal stopPrice;
    private BigDecimal maxDailyPrice;
    private String brand;
    private String model;
    private String plates;

    @JsonIgnore
    public Double getRangeInMeters() {
        if (range != null)
            return range * 1000;
        else
            return null;
    }
}
