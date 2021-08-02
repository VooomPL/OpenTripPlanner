package org.opentripplanner.prediction_client;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.locationtech.jts.geom.Envelope;

import java.util.List;

@Data
public class VehiclePresence {

    @JsonProperty("vehicle")
    private String vehicleType;
    private double cellLength;
    private double cellWidth;
    private String city;
    private CityBound cityBounds;
    private List<Prediction> predictions_15;
    private List<Prediction> predictions_30;
    private List<Prediction> predictions_45;
    private long timestamp;

    @Data
    @AllArgsConstructor
    public static class Prediction {
        private double lon;
        private double lat;
        // probability that vehicle is present at this area
        private double value;

        @JsonIgnore
        private Envelope envelope;
    }

    @Data
    static class CityBound {
        private double lon_min;
        private double lon_max;
        private double lat_min;
        private double lat_max;
    }
}
