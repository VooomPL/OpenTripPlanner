package org.opentripplanner.updater.traficstreetupdater;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.opentripplanner.hasura_client.hasura_objects.HasuraObject;

@Data
@EqualsAndHashCode(callSuper = true)
public class EdgeDataWithSpeed extends HasuraObject {

    private long id;
    private long startnodeid;
    private  long endnodeid;
    private int speed;
}
