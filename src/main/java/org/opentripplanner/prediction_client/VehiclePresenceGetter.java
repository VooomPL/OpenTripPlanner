package org.opentripplanner.prediction_client;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.locationtech.jts.geom.Envelope;
import org.opentripplanner.hasura_client.HasuraGetter;
import org.opentripplanner.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class VehiclePresenceGetter {

    private static final Logger LOG = LoggerFactory.getLogger(HasuraGetter.class);

    public Optional<VehiclePresence> getPrediction(String url, Map<String, String> params) {
        URI uri;
        try {
            uri = new URIBuilder(url).addParameters(
                    params.entrySet().stream().map(it -> new BasicNameValuePair(it.getKey(), it.getValue())).collect(toList())
            ).build();
        } catch (URISyntaxException e) {
            LOG.error("Cannot construct uri for fetching vehicle presence prediction heatmap");
            return Optional.empty();
        }
        VehiclePresence response = HttpUtils.getData(uri, new TypeReference<>() {}, 120000);
        LOG.info("Got {} objects from API", response != null ?
                response.getPredictions_15().size() + response.getPredictions_30().size() + response.getPredictions_45().size()
                : "null");
        return Optional.ofNullable(response).map(this::mapVehiclePresence);
    }

    VehiclePresence mapVehiclePresence(VehiclePresence vehiclePresence) {
        vehiclePresence.getPredictions_15().forEach(
                it -> it.setEnvelope(new Envelope(it.getLon(), it.getLon() + vehiclePresence.getCellWidth(),
                        it.getLat(), it.getLat() + vehiclePresence.getCellLength())
                ));
        vehiclePresence.getPredictions_30().forEach(
                it -> it.setEnvelope(new Envelope(it.getLon(), it.getLon() + vehiclePresence.getCellWidth(),
                        it.getLat(), it.getLat() + vehiclePresence.getCellLength())
                ));
        vehiclePresence.getPredictions_45().forEach(
                it -> it.setEnvelope(new Envelope(it.getLon(), it.getLon() + vehiclePresence.getCellWidth(),
                        it.getLat(), it.getLat() + vehiclePresence.getCellLength())
                ));
        return vehiclePresence;
    }
}
