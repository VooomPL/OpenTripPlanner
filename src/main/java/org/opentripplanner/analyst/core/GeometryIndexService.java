package org.opentripplanner.analyst.core;

import org.locationtech.jts.geom.Envelope;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.util.List;

public interface GeometryIndexService {

    @SuppressWarnings("rawtypes")
    List queryPedestrian(Envelope env);

    BoundingBox getBoundingBox(CoordinateReferenceSystem crs);
}
