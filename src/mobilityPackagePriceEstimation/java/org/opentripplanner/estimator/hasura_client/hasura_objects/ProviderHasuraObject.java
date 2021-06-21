package org.opentripplanner.estimator.hasura_client.hasura_objects;

import lombok.Getter;
import lombok.Setter;
import org.opentripplanner.hasura_client.hasura_objects.HasuraObject;
//TODO: Paulina Adamska VMP-239 Move this from simulator-associated to the main OTP hasura_client package
@Getter
@Setter
public class ProviderHasuraObject extends HasuraObject {

    private int id;
    private String name;

}
