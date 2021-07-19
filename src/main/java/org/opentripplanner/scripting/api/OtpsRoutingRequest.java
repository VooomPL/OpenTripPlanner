package org.opentripplanner.scripting.api;

import org.opentripplanner.api.parameter.QualifiedModeSet;
import org.opentripplanner.common.model.GenericLocation;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.util.DateUtils;

import java.time.Instant;
import java.time.ZonedDateTime;

/**
 * Routing request options: date/time, modes, max walk distance...
 *
 * Example of code (python script):
 *
 * <pre>
 *   router = otp.getRouter()
 *   req = otp.createRequest()
 *   req.setDateTime(2015, 1, 15, 14, 00, 00)
 *   req.setModes('WALK,BUS,RAIL')
 *   req.setOrigin(45.123, 1.456)
 *   spt = router.plan(req)
 *   ...
 * </pre>
 * 
 * @author laurent
 */
public class OtpsRoutingRequest {

    protected RoutingRequest req;

    protected OtpsRoutingRequest(RoutingRequest req) {
        this.req = req;
        req.batch = true;
    }

    public void setDateTime(int year, int month, int day, int hour, int min, int sec) {
        req.setDateTime(ZonedDateTime.of(year, month, day, hour, min, sec, 0, DateUtils.getClock().getZone()).toInstant());
    }

    public void setDateTime(Instant dateTime) {
        req.setDateTime(dateTime);
    }

    public void setDateTime(long epochSec) {
        req.setDateTime(Instant.ofEpochSecond(epochSec));
    }

    public void setModes(String modesStr) {
        new QualifiedModeSet(modesStr).applyToRoutingRequest(req);
    }

    public void setArriveBy(boolean arriveBy) {
        req.arriveBy = arriveBy;
    }

    public void setMaxTimeSec(long maxTimeSec) {
        req.worstTime = req.dateTime + (req.arriveBy ? -maxTimeSec : maxTimeSec);
    }

    public void setWalkSpeedMs(double walkSpeed) {
        req.walkSpeed = walkSpeed;
    }

    public void setBikeSpeedMs(double bikeSpeed) {
        req.bikeSpeed = bikeSpeed;
    }

    public void setMaxWalkDistance(double maxWalkDistance) {
        req.maxWalkDistance = maxWalkDistance;
    }

    public void setMaxTransferWalkDistance(double maxTransferWalkDistance) {
        req.maxTransferWalkDistance = maxTransferWalkDistance;
    }

    public void setWheelchairAccessible(boolean wheelchairAccessible) {
        req.wheelchairAccessible = wheelchairAccessible;
    }

    public void setClampInitialWait(long clampInitialWait) {
        req.clampInitialWait = clampInitialWait;
    }

    public void setOrigin(double lat, double lon) {
        req.from = new GenericLocation(lat, lon);
    }

    public void setOrigin(OtpsIndividual origin) {
        this.setOrigin(origin.lat, origin.lon);
    }

    public void setDestination(double lat, double lon) {
        req.to = new GenericLocation(lat, lon);
    }

    public void setDestination(OtpsIndividual dest) {
        this.setDestination(dest.lat, dest.lon);
    }

    public void setMaxTransfers(int maxTransfers) {
        req.maxTransfers = maxTransfers;
    }
}
