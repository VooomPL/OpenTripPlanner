package org.opentripplanner.hasura_client.mappers;

import org.opentripplanner.hasura_client.mappers.HasuraToOTPMapper;
import org.opentripplanner.updater.traficstreetupdater.EdgeDataWithSpeed;

public class EdgeDataWithSpeedMapper extends HasuraToOTPMapper<EdgeDataWithSpeed,EdgeDataWithSpeed> {

    @Override
    protected EdgeDataWithSpeed mapSingleHasuraObject(EdgeDataWithSpeed edgeDataWithSpeed) {
        return null;
    }
}
