package org.opentripplanner;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
public class Spt implements Serializable {

    private Map<String, SptElement> data = new HashMap<>();

    private String providerVehicleId;

    public void addData(String vehicleName, SptElement element) {
        if (!data.containsKey(vehicleName)) {
            data.put(vehicleName, element);
        } else {
            SptElement oldElement = data.get(vehicleName);
            if (oldElement.duration > element.duration) { // TODO
                data.put(vehicleName, element);
            }
        }
    }

    @Data
    @AllArgsConstructor
    public static class SptElement implements Serializable {

        private double distance;
        private long duration;
        private double weight;
    }
}
