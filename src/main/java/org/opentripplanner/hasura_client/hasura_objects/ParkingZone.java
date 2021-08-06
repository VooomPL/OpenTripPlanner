package org.opentripplanner.hasura_client.hasura_objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ParkingZone extends AreaForVehicleType {

    private int providerId;
    private boolean isAllowed;

    @JsonProperty("isAllowed")
    public boolean isAllowed() {
        return isAllowed;
    }
}
