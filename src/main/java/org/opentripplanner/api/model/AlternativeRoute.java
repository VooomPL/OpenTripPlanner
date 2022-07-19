package org.opentripplanner.api.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.NoArgsConstructor;
import org.opentripplanner.model.Agency;
import org.opentripplanner.model.FeedScopedId;
import org.opentripplanner.model.Route;
import org.opentripplanner.model.Trip;

@NoArgsConstructor
public class AlternativeRoute {

    @JsonSerialize
    public String route;

    @JsonSerialize
    public String agencyName;

    @JsonSerialize
    public String agencyUrl;

    @JsonSerialize
    public String agencyBrandingUrl;

    @JsonSerialize
    public String routeColor;

    @JsonSerialize
    public Integer routeType;

    @JsonSerialize
    public FeedScopedId routeId;

    @JsonSerialize
    public String routeTextColor;

    @JsonSerialize
    public String tripShortName;

    @JsonSerialize
    public String tripBlockId;

    @JsonSerialize
    public String headsign;

    @JsonSerialize
    public String agencyId;

    @JsonSerialize
    public String routeBrandingUrl;

    @JsonSerialize
    public String routeShortName;

    @JsonSerialize
    public String routeLongName;

    public AlternativeRoute(Trip trip) {
        Route route = trip.getRoute();
        Agency agency = route.getAgency();

        agencyId = agency.getId();
        agencyName = agency.getName();
        agencyUrl = agency.getUrl();
        agencyBrandingUrl = agency.getBrandingUrl();
        tripShortName = trip.getTripShortName();
        tripBlockId = trip.getBlockId();
        headsign = trip.getTripHeadsign();
        this.route = route.getShortName();
        if (this.route == null)
            this.route = route.getLongName();
        routeColor = route.getColor();
        routeId = route.getId();
        routeLongName = route.getLongName();
        routeShortName = route.getShortName();
        routeTextColor = route.getTextColor();
        routeType = route.getType();
        routeBrandingUrl = route.getBrandingUrl();
    }
}
