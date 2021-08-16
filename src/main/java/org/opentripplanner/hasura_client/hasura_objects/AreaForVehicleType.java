package org.opentripplanner.hasura_client.hasura_objects;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AreaForVehicleType extends HasuraObject {

    private String vehicleType;
    private Area area;
}
