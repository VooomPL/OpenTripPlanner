package org.opentripplanner.estimator.hasura_client.hasura_objects;

import lombok.Getter;
import lombok.Setter;
import org.opentripplanner.hasura_client.hasura_objects.HasuraObject;
import org.opentripplanner.hasura_client.hasura_objects.Vehicle;
import org.opentripplanner.routing.core.vehicle_sharing.Provider;

import java.math.BigDecimal;

@Getter
@Setter
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
