package org.opentripplanner.gtfs;

import org.opentripplanner.graph_builder.module.GtfsFeedId;
import org.opentripplanner.model.CalendarService;
import org.opentripplanner.model.OtpTransitService;

public interface GtfsContext {
    GtfsFeedId getFeedId();

    OtpTransitService getOtpTransitService();

    CalendarService getCalendarService();
}
