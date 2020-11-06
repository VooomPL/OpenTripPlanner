package org.opentripplanner.hasura_client.mappers;

import org.locationtech.jts.geom.Envelope;
import org.opentripplanner.hasura_client.hasura_objects.VehiclePresence;

public class VehiclePresenceMapper extends HasuraToOTPMapper<VehiclePresence, VehiclePresence> {

    @Override
    protected VehiclePresence mapSingleHasuraObject(VehiclePresence vehiclePresence) {
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
