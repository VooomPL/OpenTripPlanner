package org.opentripplanner.hasura_client.hasura_objects;

public class AreaForVehicleType extends HasuraObject {

    private String vehicleType;
    private Area area;

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }
}
