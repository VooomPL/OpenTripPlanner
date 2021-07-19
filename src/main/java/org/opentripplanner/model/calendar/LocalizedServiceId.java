/* This file is based on code copied from project OneBusAway, see the LICENSE file for further information. */
package org.opentripplanner.model.calendar;

import lombok.EqualsAndHashCode;
import org.opentripplanner.model.FeedScopedId;
import org.opentripplanner.model.CalendarService;

import java.io.Serializable;
import java.time.ZoneId;
import java.util.TimeZone;

/**
 * Combines a serviceId (represented by {@link FeedScopedId}) and a
 * {@link TimeZone} object that can be used to ground {@link ServiceDate}
 * objects. See {@link ServiceDate#getAsInstant(ZoneId)} for more info.
 *
 * @author bdferris
 *
 * @see ServiceDate
 * @see CalendarService
 */
@EqualsAndHashCode
public class LocalizedServiceId implements Serializable, Comparable<LocalizedServiceId> {

    private static final long serialVersionUID = 1L;

    private final FeedScopedId id;

    private final ZoneId zoneId;

    public LocalizedServiceId(FeedScopedId serviceId, ZoneId zoneId) {
        if (serviceId == null)
            throw new IllegalArgumentException("serviceId cannot be null");
        if (zoneId == null)
            throw new IllegalArgumentException("timeZone cannot be null");
        this.id = serviceId;
        this.zoneId = zoneId.normalized();
    }

    public FeedScopedId getId() {
        return id;
    }

    public ZoneId getTimeZone() {
        return zoneId;
    }

    @Override
    public String toString() {
        return "ServiceId(id=" + id + " timeZone=" + getTimeZone().getId() + ")";
    }

    @Override
    public int compareTo(LocalizedServiceId o) {
        int rc = this.id.compareTo(o.id);
        if (rc == 0)
            rc = this.zoneId.getId().compareTo(o.getTimeZone().getId());
        return rc;
    }
}
