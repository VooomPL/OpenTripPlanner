package org.opentripplanner.hasura_client.mappers;

import org.opentripplanner.hasura_client.hasura_objects.CityGovDropoffStationHasura;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleType;
import org.opentripplanner.routing.edgetype.rentedgetype.CityGovDropoffStation;

public class CityGovDropoffStationMapper extends HasuraToOTPMapper<CityGovDropoffStationHasura, CityGovDropoffStation> {

    @Override
    protected CityGovDropoffStation mapSingleHasuraObject(CityGovDropoffStationHasura station) {
        VehicleType vehicleType = VehicleType.fromDatabaseVehicleType(station.getVehicleType());
        if (vehicleType == null) {
            return null;
        }
        return new CityGovDropoffStation(station.getLongitude(), station.getLatitude(), vehicleType);
    }
}
