package org.opentripplanner.common;

import org.opentripplanner.util.DateUtils;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Represents a repeating time period, used for opening hours &c.
 * For instance: Monday - Friday 8AM to 8PM, Satuday 10AM to 5PM, Sunday closed.
 * For now it is week-based so doesn't handle every possible case, but since it is encapsulated 
 * that could conceivably be changed.
 * 
 * @author mattwigway
 *
 */
public class RepeatingTimePeriod implements Serializable {
    private static final long serialVersionUID = -5977328371879835782L;
    
    private RepeatingTimePeriod () {
        this.zoneId = null;
    }
    
    /** 
     * This stores the time periods this is active/open, stored as seconds from noon
     * (positive or negative) on the given day.
     */
    private int[][] monday;
    private int[][] tuesday;
    private int[][] wednesday;
    private int[][] thursday;
    private int[][] friday;
    private int[][] saturday;
    private int[][] sunday;
    
    /**
     * The timezone this is represented in.
     */
    private ZoneId zoneId;
   
    /**
     * Parse the time specification from an OSM turn restriction
     * @param day_on
     * @param day_off
     * @param hour_on
     * @param hour_off
     * @return
     */
    public static RepeatingTimePeriod parseFromOsmTurnRestriction (String day_on, String day_off, 
            String hour_on, String hour_off) {
        // first, create the opening and closing times. This is easy because there is the same one
        // every day of the week that this restriction is in force.
        String[] parsedOn = hour_on.split(";");
        String[] parsedOff = hour_off.split(";");
        if (parsedOn.length != parsedOff.length)
            return null;
        
        int[][] onOff = new int[parsedOn.length][];
        
        for (int i = 0; i < parsedOn.length; i++) {
            onOff[i] = new int[] {parseHour(parsedOn[i]), parseHour(parsedOff[i])};
        }
             
        boolean active = false;
        RepeatingTimePeriod ret = new RepeatingTimePeriod();
        
        // loop through twice to handle cases like Saturday - Tuesday
        for (String today : new String[] {"monday", "tuesday", "wednesday", "thursday", "friday",
                "saturday", "sunday", "monday", "tuesday", "wednesday", "thursday", "friday",
                "saturday", "sunday"}) {
            
            if (today.startsWith(day_on.toLowerCase()))
                active = true;
            
            if (active) {
                if (today.equals("monday"))
                    ret.monday = onOff;
                
                else if (today.equals("tuesday"))
                    ret.tuesday = onOff;
        
                else if (today.equals("wednesday"))
                    ret.wednesday = onOff;
                    
                else if (today.equals("thursday"))
                    ret.thursday = onOff;
                
                else if (today.equals("friday"))
                    ret.friday = onOff;
                
                else if (today.equals("saturday"))
                    ret.saturday = onOff;
                
                else if (today.equals("sunday"))
                    ret.sunday = onOff;
            }
                
            if (today.startsWith(day_off.toLowerCase()))
                active = false;
        }
        
        return ret;
    }
    
    /**
     * Return seconds before or after noon for the given hour. 
     * @param hour
     */
    private static int parseHour(String hour) {
        String[] parsed = hour.split(":");
        int ret = Integer.parseInt(parsed[0]) * 3600;
        
        if (parsed.length >= 2) {
            ret += Integer.parseInt(parsed[1]) * 60;
        }
        
        // subtract 12 hours to make it noon-relative. This implicitly handles DST.
        ret -= 12 * 3600;
        
        return ret;
    }

    public boolean active(long time) {
        // TODO: Timezone/locale
        ZonedDateTime zonedDateTime;
        // TODO offsets
        if (this.zoneId != null)
            zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(time), zoneId);
        else
            // FIXME hardwired time zone
            zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(time), DateUtils.getClock().getZone());
        
        DayOfWeek dayOfWeek = zonedDateTime.getDayOfWeek();
        
        int[][] times = null;
                
        switch(dayOfWeek) {
        case MONDAY:
            times = monday;
            break;
        case TUESDAY:
            times = tuesday;
            break;
        case WEDNESDAY:
            times = wednesday;
            break;
        case THURSDAY:
            times = thursday;
            break;
        case FRIDAY:
            times = friday;
            break;
        case SATURDAY:
            times = saturday;
            break;
        case SUNDAY:
            times = sunday;
            break;
        }

        if (times == null) {
            //no restriction today
            return false;
        }
        int timeOfDay = zonedDateTime.getHour()* 3600 + zonedDateTime.getMinute() * 60 +
                zonedDateTime.getSecond() - 12 * 3600;
        
        for (int[] range : times) {
            if (timeOfDay >= range[0] && timeOfDay <= range[1])
                return true;
        }
        
        return false;   
    }
}
