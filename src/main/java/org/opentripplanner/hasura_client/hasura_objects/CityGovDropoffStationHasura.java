package org.opentripplanner.hasura_client.hasura_objects;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CityGovDropoffStationHasura extends HasuraObject {

    private double longitude;
    private double latitude;
    private String vehicleType;
}
