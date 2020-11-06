package org.opentripplanner.hasura_client.hasura_objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;

import java.util.List;

public class VehiclePresence extends HasuraObject {

    private String vehicle;
    private double cellLength;
    private double cellWidth;
    private String city;
    private CityBound cityBounds;
    private List<Prediction> predictions_15;
    private List<Prediction> predictions_30;
    private List<Prediction> predictions_45;
    private long time;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getVehicle() {
        return vehicle;
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

    public double getCellLength() {
        return cellLength;
    }

    public void setCellLength(double cellLength) {
        this.cellLength = cellLength;
    }

    public double getCellWidth() {
        return cellWidth;
    }

    public void setCellWidth(double cellWidth) {
        this.cellWidth = cellWidth;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public CityBound getCityBounds() {
        return cityBounds;
    }

    public void setCityBounds(CityBound cityBounds) {
        this.cityBounds = cityBounds;
    }

    public List<Prediction> getPredictions_15() {
        return predictions_15;
    }

    public void setPredictions_15(List<Prediction> predictions_15) {
        this.predictions_15 = predictions_15;
    }

    public List<Prediction> getPredictions_30() {
        return predictions_30;
    }

    public void setPredictions_30(List<Prediction> predictions_30) {
        this.predictions_30 = predictions_30;
    }

    public List<Prediction> getPredictions_45() {
        return predictions_45;
    }

    public void setPredictions_45(List<Prediction> predictions_45) {
        this.predictions_45 = predictions_45;
    }

    public static class Prediction {
        private double lon;
        private double lat;
        private double value;

        @JsonIgnore
        private Envelope envelope;

        public Prediction() {

        }

        public Prediction(double lon, double lat, double value, Envelope envelope) {
            this.lon = lon;
            this.lat = lat;
            this.value = value;
            this.envelope = envelope;
        }

        public Envelope getEnvelope() {
            return envelope;
        }

        public void setEnvelope(Envelope envelope) {
            this.envelope = envelope;
        }

        public double getLon() {
            return lon;
        }

        public void setLon(double lon) {
            this.lon = lon;
        }

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }

    }

    static class CityBound {
        private double lon_min;
        private double lon_max;
        private double lat_min;
        private double lat_max;

        public double getLon_min() {
            return lon_min;
        }

        public void setLon_min(double lon_min) {
            this.lon_min = lon_min;
        }

        public double getLon_max() {
            return lon_max;
        }

        public void setLon_max(double lon_max) {
            this.lon_max = lon_max;
        }

        public double getLat_min() {
            return lat_min;
        }

        public void setLat_min(double lat_min) {
            this.lat_min = lat_min;
        }

        public double getLat_max() {
            return lat_max;
        }

        public void setLat_max(double lat_max) {
            this.lat_max = lat_max;
        }
    }

}
