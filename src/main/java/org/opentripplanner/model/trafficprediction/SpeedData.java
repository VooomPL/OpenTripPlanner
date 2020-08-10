package org.opentripplanner.model.trafficprediction;

public class SpeedData {

    private int id;
    private long clusterId;
    private int currentSpeed;
    private String measureTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getClusterId() {
        return clusterId;
    }

    public void setClusterId(long clusterId) {
        this.clusterId = clusterId;
    }

    public int getCurrentSpeed() {
        return currentSpeed;
    }

    public void setCurrentSpeed(int currentSpeed) {
        this.currentSpeed = currentSpeed;
    }

    public String getMeasureTime() {
        return measureTime;
    }

    public void setMeasureTime(String measureTime) {
        this.measureTime = measureTime;
    }
}
