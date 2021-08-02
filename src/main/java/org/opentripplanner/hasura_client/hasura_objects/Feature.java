package org.opentripplanner.hasura_client.hasura_objects;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Feature extends HasuraObject {

    private String type;
    private Object properties;
    private JsonNode geometry;
}
