package org.opentripplanner.estimator.hasura_client.hasura_objects;

import org.opentripplanner.hasura_client.hasura_objects.HasuraObject;

public class ProviderHasuraObject extends HasuraObject {

    private int id;
    private String name;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
