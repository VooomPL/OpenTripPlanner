package org.opentripplanner.api.adapters;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.opentripplanner.util.PolylineEncoder;
import org.opentripplanner.util.model.EncodedPolylineBean;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Arrays;
import java.util.List;

public class LineStringAdapter extends XmlAdapter<EncodedPolylineBean, LineString> {

    @Override
    public LineString unmarshal(EncodedPolylineBean arg) throws Exception {
        throw new UnsupportedOperationException("We presently serialize LineString as EncodedPolylineBean, and thus cannot deserialize them");
    }

    @Override
    public EncodedPolylineBean marshal(LineString arg) throws Exception {
        if (arg == null) {
            return null;
        }
        Coordinate[] lineCoords = arg.getCoordinates();
        List<Coordinate> coords = Arrays.asList(lineCoords);
        return PolylineEncoder.createEncodings(coords);
    }


}
