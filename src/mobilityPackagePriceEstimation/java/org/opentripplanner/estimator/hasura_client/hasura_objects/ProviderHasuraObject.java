package org.opentripplanner.estimator.hasura_client.hasura_objects;

import lombok.Getter;
import lombok.Setter;
import org.opentripplanner.hasura_client.hasura_objects.HasuraObject;

@Getter
@Setter
public class ProviderHasuraObject extends HasuraObject {

    private int id;
    private String name;

}
