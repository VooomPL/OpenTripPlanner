package org.opentripplanner.hasura_client.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import org.geotools.geojson.geom.GeometryJSON;
import org.locationtech.jts.geom.Geometry;
import org.opentripplanner.hasura_client.hasura_objects.Area;
import org.opentripplanner.hasura_client.hasura_objects.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

public class AreaMapper extends HasuraToOTPMapper<Area, Geometry> {

    private static final Logger LOG = LoggerFactory.getLogger(AreaMapper.class);

    private final GeometryJSON geometryJSON = new GeometryJSON();

    @Override
    protected Geometry mapSingleHasuraObject(Area area) {
        // This feature requires custom list mapping, one area maps to many geometries
        throw new NotImplementedException();
    }

    private Geometry deserializeGeometry(JsonNode jsonObject) {
        try {
            return geometryJSON.read(jsonObject.toString());
        } catch (Exception e) {
            LOG.warn("Failed to deserialize GeometryJSON", e);
            return null;
        }
    }

    @Override
    public List<Geometry> map(List<Area> areas) {
        return areas.stream()
                .map(Area::getFeatures)
                .flatMap(Collection::stream)
                .map(Feature::getGeometry)
                .map(this::deserializeGeometry)
                .filter(Objects::nonNull)
                .collect(toList());
    }
}
