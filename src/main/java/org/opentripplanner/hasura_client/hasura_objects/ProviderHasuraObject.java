package org.opentripplanner.hasura_client.hasura_objects;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProviderHasuraObject extends HasuraObject {

    private int id;
    private String name;

}
