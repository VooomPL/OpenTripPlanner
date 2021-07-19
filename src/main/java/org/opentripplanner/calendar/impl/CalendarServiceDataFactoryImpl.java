/* This file is based on code copied from project OneBusAway, see the LICENSE file for further information. */
package org.opentripplanner.calendar.impl;

import org.opentripplanner.model.*;
import org.opentripplanner.model.calendar.CalendarServiceData;
import org.opentripplanner.model.calendar.LocalizedServiceId;
import org.opentripplanner.model.calendar.ServiceDate;
import org.opentripplanner.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * We perform initial date calculations in the timezone of the host jvm, which
 * may be different than the timezone of an agency with the specified service
 * id. To my knowledge, the calculation should work the same, which is to say I
 * can't immediately think of any cases where the service dates would be
 * computed incorrectly.
 *
 * @author bdferris
 */
public class CalendarServiceDataFactoryImpl {

    private static final Logger LOG = LoggerFactory.getLogger(CalendarServiceDataFactoryImpl.class);

    private final OtpTransitService transitService;

    public static CalendarService createCalendarService(OtpTransitService transitService) {
        return new CalendarServiceImpl(createCalendarServiceData(transitService));
    }

    public static CalendarServiceData createCalendarServiceData(OtpTransitService transitService) {
        return new CalendarServiceDataFactoryImpl(transitService).createData();
    }

    public static CalendarServiceData createCalendarSrvDataWithoutDatesForLocalizedSrvId(OtpTransitService transitService) {
        return (new CalendarServiceDataFactoryImpl(transitService) {
            @Override
            void addDatesForLocalizedServiceId(
                    FeedScopedId serviceId, List<ServiceDate> serviceDates, CalendarServiceData data
            ) {
                // Skip creation of datesForLocalizedServiceId
            }
        }).createData();
    }

    private CalendarServiceDataFactoryImpl(OtpTransitService transitService) {
        this.transitService = transitService;
    }

    CalendarServiceData createData() {

        CalendarServiceData data = new CalendarServiceData();

        setTimeZonesForAgencies(data);

        List<FeedScopedId> serviceIds = transitService.getAllServiceIds();

        int index = 0;

        for (FeedScopedId serviceId : serviceIds) {

            index++;

            LOG.debug("serviceId=" + serviceId + " (" + index + "/" + serviceIds.size() + ")");

            ZoneId serviceIdTimeZone = data.getTimeZoneForAgencyId(serviceId.getAgencyId());
            if (serviceIdTimeZone == null) {
                serviceIdTimeZone = DateUtils.getClock().getZone();
            }

            Set<ServiceDate> activeDates = getServiceDatesForServiceId(serviceId,
                    serviceIdTimeZone);

            List<ServiceDate> serviceDates = new ArrayList<>(activeDates);
            Collections.sort(serviceDates);

            data.putServiceDatesForServiceId(serviceId, serviceDates);

            addDatesForLocalizedServiceId(serviceId, serviceDates, data);
        }

        return data;
    }

    void addDatesForLocalizedServiceId(
            FeedScopedId serviceId, List<ServiceDate> serviceDates, CalendarServiceData data) {
        List<String> tripAgencyIds = transitService.getTripAgencyIdsReferencingServiceId(serviceId);
        Set<ZoneId> timeZones = new HashSet<>();
        for (String tripAgencyId : tripAgencyIds) {
            ZoneId timeZone = data.getTimeZoneForAgencyId(tripAgencyId);
            timeZones.add(timeZone);
        }

        for (ZoneId zoneId : timeZones) {

            List<Instant> dates = new ArrayList<>(serviceDates.size());
            for (ServiceDate serviceDate : serviceDates)
                dates.add(serviceDate.getAsInstant(zoneId));

            LocalizedServiceId id = new LocalizedServiceId(serviceId, zoneId);
            data.putDatesForLocalizedServiceId(id, dates);
        }
    }

    private Set<ServiceDate> getServiceDatesForServiceId(FeedScopedId serviceId,
                                                         ZoneId serviceIdTimeZone) {
        Set<ServiceDate> activeDates = new HashSet<>();
        ServiceCalendar c = transitService.getCalendarForServiceId(serviceId);

        if (c != null) {
            addDatesFromCalendar(c, serviceIdTimeZone, activeDates);
        }
        for (ServiceCalendarDate cd : transitService.getCalendarDatesForServiceId(serviceId)) {
            addAndRemoveDatesFromCalendarDate(cd, activeDates);
        }
        return activeDates;
    }

    private void setTimeZonesForAgencies(CalendarServiceData data) {
        for (Agency agency : transitService.getAllAgencies()) {
            ZoneId timeZone = ZoneId.of(agency.getTimezone());
            if (timeZone.getId().equals("GMT") && !agency.getTimezone().equalsIgnoreCase("GMT")) {
                throw new UnknownAgencyTimezoneException(agency.getName(), agency.getTimezone());
            }
            data.putTimeZoneForAgencyId(agency.getId(), timeZone);
        }
    }

    private void addDatesFromCalendar(ServiceCalendar calendar, ZoneId zoneId,
                                      Set<ServiceDate> activeDates) {

        // We calculate service dates relative to noon so as to avoid any weirdness
        // relative to DST.
        Instant startDate = getServiceDateAsNoon(calendar.getStartDate(), zoneId);
        Instant endDate = getServiceDateAsNoon(calendar.getEndDate(), zoneId);

        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(startDate, zoneId);

        while (true) {
            Instant date = zonedDateTime.toInstant();
            if (date.isAfter(endDate))
                break;

            DayOfWeek day = zonedDateTime.getDayOfWeek();
            boolean active = false;

            switch (day) {
                case MONDAY:
                    active = calendar.getMonday() == 1;
                    break;
                case TUESDAY:
                    active = calendar.getTuesday() == 1;
                    break;
                case WEDNESDAY:
                    active = calendar.getWednesday() == 1;
                    break;
                case THURSDAY:
                    active = calendar.getThursday() == 1;
                    break;
                case FRIDAY:
                    active = calendar.getFriday() == 1;
                    break;
                case SATURDAY:
                    active = calendar.getSaturday() == 1;
                    break;
                case SUNDAY:
                    active = calendar.getSunday() == 1;
                    break;
            }

            if (active) {
                addServiceDate(activeDates, new ServiceDate(zonedDateTime));
            }

            zonedDateTime = zonedDateTime.plusDays(1);
        }
    }

    private void addAndRemoveDatesFromCalendarDate(ServiceCalendarDate calendarDate,
                                                   Set<ServiceDate> activeDates) {
        ServiceDate serviceDate = calendarDate.getDate();
        switch (calendarDate.getExceptionType()) {
            case ServiceCalendarDate.EXCEPTION_TYPE_ADD:
                addServiceDate(activeDates, serviceDate);
                break;
            case ServiceCalendarDate.EXCEPTION_TYPE_REMOVE:
                activeDates.remove(serviceDate);
                break;
            default:
                LOG.warn("unknown CalendarDate exception type: " + calendarDate.getExceptionType());
                break;
        }
    }

    private void addServiceDate(Set<ServiceDate> activeDates, ServiceDate serviceDate) {
        activeDates.add(new ServiceDate(serviceDate));
    }

    private static Instant getServiceDateAsNoon(ServiceDate serviceDate, ZoneId zoneId) {
        ZonedDateTime c = serviceDate.getAsZonedDateTime(zoneId);
        return c.plusHours(12).toInstant();
    }
}