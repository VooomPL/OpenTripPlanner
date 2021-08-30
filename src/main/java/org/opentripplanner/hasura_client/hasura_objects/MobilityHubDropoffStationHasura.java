package org.opentripplanner.hasura_client.hasura_objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MobilityHubDropoffStationHasura extends HasuraObject {

    private double longitude;
    private double latitude;
    private String vehicleType;
    private int providerId;
}
