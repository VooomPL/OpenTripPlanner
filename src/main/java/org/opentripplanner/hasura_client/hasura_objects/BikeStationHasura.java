package org.opentripplanner.hasura_client.hasura_objects;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.opentripplanner.routing.core.vehicle_sharing.Provider;

@Data
@EqualsAndHashCode(callSuper = true)
public class BikeStationHasura extends HasuraObject {

    private int bikesAvailable;
    private int spacesAvailable;
    private Provider provider;
    private double longitude;
    private double latitude;
    private String providerStationId;
    private String name;
}
