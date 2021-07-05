package org.opentripplanner.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Instant;

/**
 * @author Frank Purcell (p u r c e l l f @ t r i m e t . o r g)
 * @date October 20, 2009
 */
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
