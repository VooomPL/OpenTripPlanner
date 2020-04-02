package org.opentripplanner.routing.edgetype;

import org.locationtech.jts.geom.LineString;
import org.opentripplanner.model.Stop;

/**
 * FrequencyHops and PatternHops have start/stop Stops
 *
 * @author novalis
 */
public interface HopEdge {

    Stop getEndStop();

    Stop getBeginStop();

    void setGeometry(LineString geometry);

    String getFeedId();
}
