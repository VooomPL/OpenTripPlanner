package org.opentripplanner.routing.edgetype.rentedgetype;

import lombok.Value;

import java.util.List;

@Value
public class MobilityHubDropoffStation {

    double longitude, latitude;
    List<SingleParkingZone> parkingZones;
}
