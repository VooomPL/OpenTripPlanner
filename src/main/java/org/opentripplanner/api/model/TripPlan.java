package org.opentripplanner.api.model;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A TripPlan is a set of ways to get from point A to point B at time T.
 */
public class TripPlan {

    /**  The time and date of travel as a timestamp with millis */
    public Long date = null;
    
    /** The origin */
    public Place from = null;
    
    /** The destination */
    public Place to = null;

    /** A list of possible itineraries */
    @JsonProperty(value="itineraries")
    public List<Itinerary> itinerary = new ArrayList<Itinerary>();

    public TripPlan() { }

    public TripPlan(Place from, Place to, long date) {
        this.from = from;
        this.to = to;
        this.date = date;
    }

    public void addItinerary(Itinerary itinerary) {
        this.itinerary.add(itinerary);
    }
}
