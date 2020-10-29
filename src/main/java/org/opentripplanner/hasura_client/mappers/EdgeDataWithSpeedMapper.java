package org.opentripplanner.hasura_client.mappers;

import org.opentripplanner.graph_builder.module.time.EdgeData;
import org.opentripplanner.hasura_client.mappers.HasuraToOTPMapper;
import org.opentripplanner.updater.traficstreetupdater.EdgeDataWithSpeed;

public class EdgeDataWithSpeedMapper extends HasuraToOTPMapper<EdgeDataWithSpeed,EdgeDataWithSpeed> {

    @Override
    public EdgeDataWithSpeed mapSingleHasuraObject(EdgeDataWithSpeed hasuraObject) {
            return hasuraObject;
    }
}
