package org.opentripplanner.routing.bike_rental;

import org.opentripplanner.routing.bike_park.BikePark;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class BikeRentalStationService implements Serializable {
    private static final long serialVersionUID = -1288992939159246764L;

    private Set<BikeRentalStation> bikeRentalStations = new HashSet<>();

    private Set<BikePark> bikeParks = new HashSet<>();

    public Collection<BikeRentalStation> getBikeRentalStations() {
        return bikeRentalStations;
    }

    public void addBikeRentalStation(BikeRentalStation bikeRentalStation) {
        // Remove old reference first, as adding will be a no-op if already present
        bikeRentalStations.remove(bikeRentalStation);
        bikeRentalStations.add(bikeRentalStation);
    }

    public void removeBikeRentalStation(BikeRentalStation bikeRentalStation) {
        bikeRentalStations.remove(bikeRentalStation);
    }

    public Collection<BikePark> getBikeParks() {
        return bikeParks;
    }

    public void addBikePark(BikePark bikePark) {
        // Remove old reference first, as adding will be a no-op if already present
        bikeParks.remove(bikePark);
        bikeParks.add(bikePark);
    }

    public void removeBikePark(BikePark bikePark) {
        bikeParks.remove(bikePark);
    }
}
