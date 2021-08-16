package org.opentripplanner.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Instant;

public class DateUtils {

    static Clock CLOCK = Clock.systemUTC();

    public static void replace_clock(Clock clock) {
        CLOCK = clock;
    }

    public static Clock getClock() {
        return CLOCK;
    }

    public static Instant getInstant() {
        return CLOCK.instant();
    }

    private static final Logger LOG = LoggerFactory.getLogger(DateUtils.class);

    public static long absoluteTimeout(double relativeTimeoutSeconds) {
        if (relativeTimeoutSeconds <= 0)
            return Long.MAX_VALUE;
        else
            return CLOCK.millis() + (long) (relativeTimeoutSeconds * 1000.0);
    }
}
