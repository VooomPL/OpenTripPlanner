package org.opentripplanner.model.trafficprediction;

public class EdgeData {

    private long id;
    private int clusterId;

    private long startNodeId;
    private double startLatitude;
    private long startLongitude;

    private long endNodeId;
    private double endLatitude;
    private long endLongitude;

    private long wayId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getClusterId() {
        return clusterId;
    }

    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
    }

    public long getStartNodeId() {
        return startNodeId;
    }

    public void setStartNodeId(long startNodeId) {
        this.startNodeId = startNodeId;
    }

    public double getStartLatitude() {
        return startLatitude;
    }

    public void setStartLatitude(double startLatitude) {
        this.startLatitude = startLatitude;
    }

    public long getStartLongitude() {
        return startLongitude;
    }

    public void setStartLongitude(long startLongitude) {
        this.startLongitude = startLongitude;
    }

    public long getEndNodeId() {
        return endNodeId;
    }

    public void setEndNodeId(long endNodeId) {
        this.endNodeId = endNodeId;
    }

    public double getEndLatitude() {
        return endLatitude;
    }

    public void setEndLatitude(double endLatitude) {
        this.endLatitude = endLatitude;
    }

    public long getEndLongitude() {
        return endLongitude;
    }

    public void setEndLongitude(long endLongitude) {
        this.endLongitude = endLongitude;
    }

    public long getWayId() {
        return wayId;
    }

    public void setWayId(long wayId) {
        this.wayId = wayId;
    }
}
