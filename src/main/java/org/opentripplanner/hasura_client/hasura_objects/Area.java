package org.opentripplanner.hasura_client.hasura_objects;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class Area extends HasuraObject {

    private List<Feature> features;
    private String type;
}
