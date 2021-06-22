package org.opentripplanner.analyst.request;

import org.locationtech.jts.geom.Coordinate;

import java.util.Arrays;
import java.util.List;

/**
 * A request for an isochrone vector.
 * 
 * @author laurent
 */
public class IsoChroneRequest {

    public final List<Integer> cutoffSecList;

    public boolean includeDebugGeometry;

    public int precisionMeters = 200;

    public int offRoadDistanceMeters = 150;

    public int maxTimeSec = 0;

    public Coordinate coordinateOrigin;

    public final int minCutoffSec;

    public final int maxCutoffSec;

    public int maxDistanceMeters;

    public IsoChroneRequest(List<Integer> cutoffSecList) {
        this.cutoffSecList = cutoffSecList;
        minCutoffSec = cutoffSecList.stream().min(Integer::compareTo).orElse(Integer.MAX_VALUE);
        maxCutoffSec = cutoffSecList.stream().max(Integer::compareTo).orElse(0);
    }

    @Override
    public int hashCode() {
        return cutoffSecList.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof IsoChroneRequest) {
            IsoChroneRequest otherReq = (IsoChroneRequest) other;
            return this.cutoffSecList.equals(otherReq.cutoffSecList);
        }
        return false;
    }

    public String toString() {
        return String.format("<isochrone request, cutoff=%s sec, precision=%d meters>",
                Arrays.toString(cutoffSecList.toArray()), precisionMeters);
    }
}
