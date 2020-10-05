package org.opentripplanner.hasura_client.hasura_objects;

import com.fasterxml.jackson.databind.JsonNode;

public class Feature {

    private String type;
    private Object properties;
    private JsonNode geometry;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getProperties() {
        return properties;
    }

    public void setProperties(Object properties) {
        this.properties = properties;
    }

    public JsonNode getGeometry() {
        return geometry;
    }

    public void setGeometry(JsonNode geometry) {
        this.geometry = geometry;
    }
}
