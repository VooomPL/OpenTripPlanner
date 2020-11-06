package org.opentripplanner.prediction_client;

import org.locationtech.jts.geom.Envelope;

public class VehiclePresenceMapper {

    VehiclePresence mapSingleHasuraObject(VehiclePresence vehiclePresence) {
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
