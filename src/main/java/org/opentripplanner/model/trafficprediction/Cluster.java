package org.opentripplanner.model.trafficprediction;


public class Cluster {

    private int id;
    private SpeedData[] speed;
    private EdgeData[] edges;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public SpeedData[] getSpeed() {
        return speed;
    }

    public void setSpeed(SpeedData[] speed) {
        this.speed = speed;
    }

    public EdgeData[] getEdges() {
        return edges;
    }

    public void setEdges(EdgeData[] edges) {
        this.edges = edges;
    }
}
