package org.opentripplanner.hasura_client.hasura_objects;

import java.util.List;

public class Area extends HasuraObject {

    private List<Feature> features;
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }
}
