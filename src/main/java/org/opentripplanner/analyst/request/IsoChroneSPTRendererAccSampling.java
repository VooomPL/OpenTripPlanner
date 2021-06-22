package org.opentripplanner.analyst.request;

import org.opentripplanner.analyst.core.IsochroneData;
import org.opentripplanner.analyst.request.SampleGridRenderer.WTWD;
import org.opentripplanner.common.geometry.DelaunayIsolineBuilder;
import org.opentripplanner.common.geometry.IsolineBuilder.ZMetric;
import org.opentripplanner.common.geometry.ZSampleGrid;
import org.opentripplanner.routing.core.RoutingRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Compute isochrones out of a shortest path tree request (AccSampling isoline algorithm).
 *
 * @author laurent
 */
public class IsoChroneSPTRendererAccSampling implements IsoChroneSPTRenderer {

    private static final Logger LOG = LoggerFactory
            .getLogger(IsoChroneSPTRendererAccSampling.class);

    private SampleGridRenderer sampleGridRenderer;

    public IsoChroneSPTRendererAccSampling(SampleGridRenderer sampleGridRenderer) {
        this.sampleGridRenderer = sampleGridRenderer;
    }

    /**
     * @param isoChroneRequest
     * @param sptRequest
     * @return
     */
    @Override
    public List<IsochroneData> getIsochrones(IsoChroneRequest isoChroneRequest,
            RoutingRequest sptRequest) {

        final double offRoadDistanceMeters = isoChroneRequest.offRoadDistanceMeters;

        // 1. Create a sample grid from the SPT, using the TimeGridRenderer
        SampleGridRequest tgRequest = new SampleGridRequest();
        tgRequest.maxTimeSec = isoChroneRequest.maxTimeSec;
        tgRequest.precisionMeters = isoChroneRequest.precisionMeters;
        tgRequest.offRoadDistanceMeters = isoChroneRequest.offRoadDistanceMeters;
        tgRequest.coordinateOrigin = isoChroneRequest.coordinateOrigin;
        tgRequest.maxDistanceMeters = isoChroneRequest.maxDistanceMeters;
        ZSampleGrid<WTWD> sampleGrid = sampleGridRenderer.getSampleGrid(tgRequest, sptRequest);

        // 2. Compute isolines
        long t0 = System.currentTimeMillis();
        ZMetric<WTWD> zMetric = new ZMetric<WTWD>() {
            @Override
            public int cut(WTWD zA, WTWD zB, WTWD z0) {
                double t0 = z0.wTime / z0.w;
                double tA = zA.d > z0.d ? Double.POSITIVE_INFINITY : zA.wTime / zA.w;
                double tB = zB.d > z0.d ? Double.POSITIVE_INFINITY : zB.wTime / zB.w;
                if (tA < t0 && t0 <= tB)
                    return 1;
                if (tB < t0 && t0 <= tA)
                    return -1;
                return 0;
            }

            @Override
            public double interpolate(WTWD zA, WTWD zB, WTWD z0) {
                if (zA.d > z0.d || zB.d > z0.d) {
                    if (zA.d > z0.d && zB.d > z0.d)
                        throw new AssertionError("dA > d0 && dB > d0");
                    // Interpolate on d
                    double k = zA.d == zB.d ? 0.5 : (z0.d - zA.d) / (zB.d - zA.d);
                    return k;
                } else {
                    // Interpolate on t
                    double tA = zA.wTime / zA.w;
                    double tB = zB.wTime / zB.w;
                    double t0 = z0.wTime / z0.w;
                    double k = tA == tB ? 0.5 : (t0 - tA) / (tB - tA);
                    return k;
                }
            }
        };
        DelaunayIsolineBuilder<WTWD> isolineBuilder = new DelaunayIsolineBuilder<WTWD>(
                sampleGrid.delaunayTriangulate(), zMetric);
        isolineBuilder.setDebug(isoChroneRequest.includeDebugGeometry);

        List<IsochroneData> isochrones = new ArrayList<IsochroneData>();
        for (Integer cutoffSec : isoChroneRequest.cutoffSecList) {
            WTWD z0 = new WTWD();
            z0.w = 1.0;
            z0.wTime = cutoffSec;
            z0.d = offRoadDistanceMeters;
            IsochroneData isochrone = new IsochroneData(cutoffSec,
                    isolineBuilder.computeIsoline(z0));
            if (isoChroneRequest.includeDebugGeometry)
                isochrone.debugGeometry = isolineBuilder.getDebugGeometry();
            isochrones.add(isochrone);
        }

        long t1 = System.currentTimeMillis();
        LOG.info("Computed {} isochrones in {}msec", isochrones.size(), (int) (t1 - t0));

        return isochrones;
    }
}
