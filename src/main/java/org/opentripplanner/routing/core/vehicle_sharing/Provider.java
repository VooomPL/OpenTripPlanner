package org.opentripplanner.routing.core.vehicle_sharing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Provider {

    private int providerId;

    private String providerName;
}
