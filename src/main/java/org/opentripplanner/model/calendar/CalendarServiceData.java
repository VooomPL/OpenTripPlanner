/* This file is based on code copied from project OneBusAway, see the LICENSE file for further information. */
package org.opentripplanner.model.calendar;

import org.opentripplanner.model.FeedScopedId;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;

public class CalendarServiceData implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Map<String, ZoneId> timeZonesByAgencyId = new HashMap<>();

    private final Map<FeedScopedId, List<ServiceDate>> serviceDatesByServiceId = new HashMap<>();

    private final Map<LocalizedServiceId, List<Instant>> datesByLocalizedServiceId = new HashMap<>();

    private final Map<ServiceDate, Set<FeedScopedId>> serviceIdsByDate = new HashMap<>();

    /**
     * @param agencyId
     * @return the time zone for the specified agencyId, or null if the agency was
     *         not found
     */
    public ZoneId getTimeZoneForAgencyId(String agencyId) {
        return timeZonesByAgencyId.get(agencyId);
    }

    public void putTimeZoneForAgencyId(String agencyId, ZoneId zoneId) {
        timeZonesByAgencyId.put(agencyId, zoneId);
    }

    public Set<FeedScopedId> getServiceIds() {
        return Collections.unmodifiableSet(serviceDatesByServiceId.keySet());
    }

    public Set<LocalizedServiceId> getLocalizedServiceIds() {
        return Collections.unmodifiableSet(datesByLocalizedServiceId.keySet());
    }

    public List<ServiceDate> getServiceDatesForServiceId(FeedScopedId serviceId) {
        return serviceDatesByServiceId.get(serviceId);
    }

    public Set<FeedScopedId> getServiceIdsForDate(ServiceDate date) {
        Set<FeedScopedId> serviceIds = serviceIdsByDate.get(date);
        if (serviceIds == null)
            serviceIds = new HashSet<>();
        return serviceIds;
    }

    public void putServiceDatesForServiceId(FeedScopedId serviceId, List<ServiceDate> serviceDates) {
        serviceDates = new ArrayList<>(serviceDates);
        Collections.sort(serviceDates);
        serviceDates = Collections.unmodifiableList(serviceDates);
        serviceDatesByServiceId.put(serviceId, serviceDates);
        for (ServiceDate serviceDate : serviceDates) {
            Set<FeedScopedId> serviceIds = serviceIdsByDate.get(serviceDate);
            if (serviceIds == null) {
                serviceIds = new HashSet<>();
                serviceIdsByDate.put(serviceDate, serviceIds);
            }
            serviceIds.add(serviceId);
        }
    }

    public List<Instant> getDatesForLocalizedServiceId(LocalizedServiceId serviceId) {
        return datesByLocalizedServiceId.get(serviceId);
    }

    public void putDatesForLocalizedServiceId(LocalizedServiceId serviceId, List<Instant> dates) {
        dates = Collections.unmodifiableList(new ArrayList<>(dates));
        datesByLocalizedServiceId.put(serviceId, dates);
    }
}
