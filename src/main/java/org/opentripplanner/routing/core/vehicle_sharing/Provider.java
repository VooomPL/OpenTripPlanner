package org.opentripplanner.routing.core.vehicle_sharing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Provider {

    public Provider(int id, String name) {
        providerId = id;
        providerName = name;
    }

    private int providerId;

    private String providerName;

    private String appiOSLink;

    private String appAndroidLink;
}
