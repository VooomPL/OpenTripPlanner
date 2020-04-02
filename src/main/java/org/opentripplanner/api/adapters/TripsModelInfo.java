package org.opentripplanner.api.adapters;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.opentripplanner.model.FeedScopedId;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement(name = "trip")
public class TripsModelInfo implements Serializable {

    private static final long serialVersionUID = -4853941297409355512L;

    public TripsModelInfo(String headsign, Integer number, String calendarId, FeedScopedId tripId) {
        this.headsign = headsign;
        this.numberOfTrips = number;
        this.calendarId = calendarId;
        this.id = tripId.getId();
        this.agency = tripId.getAgencyId();
    }

    public TripsModelInfo() {
    }

    public String getId() {
        return id;
    }

    @XmlAttribute
    @JsonSerialize
    String headsign;

    @XmlAttribute
    @JsonSerialize
    Integer numberOfTrips;

    @XmlAttribute
    @JsonSerialize
    String calendarId;

    @XmlAttribute
    @JsonSerialize
    String id;

    @XmlAttribute
    @JsonSerialize
    String agency;
}
