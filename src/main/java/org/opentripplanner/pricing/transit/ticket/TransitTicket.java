package org.opentripplanner.pricing.transit.ticket;

import lombok.Getter;
import org.opentripplanner.model.Route;
import org.opentripplanner.pricing.transit.ticket.pattern.FareSwitchPattern;
import org.opentripplanner.pricing.transit.ticket.pattern.RoutePattern;
import org.opentripplanner.pricing.transit.ticket.pattern.StopPattern;
import org.opentripplanner.pricing.transit.trip.model.FareSwitch;
import org.opentripplanner.pricing.transit.trip.model.TransitTripStage;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.Objects.isNull;

public class TransitTicket {

    private enum ConstraintCategory {TIME, ROUTE_STOP_PATTERN, MAX_FARES, MAX_DISTANCE}

    private static final int NO_LIMIT = -1;

    @Getter
    private final int id;

    @Getter
    private final int maxMinutes;

    @Getter
    private final BigDecimal standardPrice;

    private final Map<String, RoutePattern> routePatterns = new HashMap<>();

    private final Map<String, StopPattern> stopPatterns = new HashMap<>();

    @Getter
    private final int maxFares;

    @Getter
    private final List<FareSwitchPattern> fareSwitchPatterns = new ArrayList<>();

    @Getter
    private final int maxDistance;

    @Getter
    private final LocalDateTime availableFrom;

    @Getter
    private final LocalDateTime availableTo;

    private TransitTicket(int id, BigDecimal standardPrice, int validForMinutes, int validForFares, int validForDistance,
                          LocalDateTime availableFrom, LocalDateTime availableTo) {
        this.id = id;
        this.standardPrice = standardPrice;
        this.maxMinutes = validForMinutes;
        this.maxFares = validForFares;
        this.maxDistance = validForDistance;
        this.availableFrom = availableFrom;
        this.availableTo = availableTo;
    }

    public static TransitTicketBuilder builder(int id, BigDecimal standardPrice) {
        return new TransitTicketBuilder(id, standardPrice);
    }

    public static final class TransitTicketBuilder {

        private final int id;
        private final BigDecimal standardPrice;
        private int validForMinutes = NO_LIMIT;
        private int validForFares = NO_LIMIT;
        private int validForDistance = NO_LIMIT;
        private LocalDateTime availableFrom = null;
        private LocalDateTime availableTo = null;

        private TransitTicketBuilder(int id, BigDecimal standardPrice) {
            this.id = id;
            this.standardPrice = standardPrice;
        }

        public TransitTicket build() {
            return new TransitTicket(this.id, this.standardPrice, this.validForMinutes, this.validForFares,
                    this.validForDistance, this.availableFrom, this.availableTo);
        }

        public TransitTicketBuilder setTimeLimit(int validForMinutes) {
            this.validForMinutes = getCorrectedLimitValue(validForMinutes);
            return this;
        }

        public TransitTicketBuilder setFaresNumberLimit(int validForFares) {
            this.validForFares = getCorrectedLimitValue(validForFares);
            return this;
        }

        public TransitTicketBuilder setDistanceLimit(int validForDistance) {
            this.validForDistance = getCorrectedLimitValue(validForDistance);
            return this;
        }

        private int getCorrectedLimitValue(int evaluatedLimit) {
            return evaluatedLimit > 0 ? evaluatedLimit : NO_LIMIT;
        }

        public TransitTicketBuilder setAvailableFrom(LocalDateTime availableFrom) {
            this.availableFrom = availableFrom;
            return this;
        }

        public TransitTicketBuilder setAvailableTo(LocalDateTime availableTo) {
            this.availableTo = availableTo;
            return this;
        }

    }

    public void addAllowedAgency(String agencyId) {
        routePatterns.put(agencyId, new RoutePattern());
        stopPatterns.put(agencyId, new StopPattern());
    }

    public RoutePattern getRoutePattern(String agencyId) {
        return routePatterns.get(agencyId);
    }

    public StopPattern getStopPattern(String agencyId) {
        return stopPatterns.get(agencyId);
    }

    public boolean isAvailable(LocalDateTime currentTimestamp) {
        return (Objects.isNull(this.availableTo) || this.availableTo.isAfter(currentTimestamp)) &&
                (Objects.isNull(this.availableFrom) || !this.availableFrom.isAfter(currentTimestamp));
    }

    public int getTotalMinutesWhenValid(int finishesAtMinute, List<TransitTripStage> tripStages) {
        if (isNull(tripStages)) return 0;

        HashMap<ConstraintCategory, Integer> totalMinutesWhenValid = new HashMap<>();
        totalMinutesWhenValid.put(ConstraintCategory.TIME, maxMinutes != NO_LIMIT ? maxMinutes : finishesAtMinute);
        totalMinutesWhenValid.put(ConstraintCategory.ROUTE_STOP_PATTERN, getRouteStopConstraintCompliantTime(finishesAtMinute, tripStages));
        totalMinutesWhenValid.put(ConstraintCategory.MAX_FARES, maxFares != NO_LIMIT ?
                getMaxFaresConstraintCompliantTime(finishesAtMinute, tripStages) : finishesAtMinute);
        totalMinutesWhenValid.put(ConstraintCategory.MAX_DISTANCE, maxDistance != NO_LIMIT ?
                getMaxDistanceConstraintCompliantTime(finishesAtMinute, tripStages) : finishesAtMinute);

        return Collections.min(totalMinutesWhenValid.values());
    }

    private int getRouteStopConstraintCompliantTime(int ticketShouldBeValidUntil, List<TransitTripStage> tripStages) {
        int totalMinutesWhenValid = 0;

        TransitTripStage evaluatedTripStage;
        TransitTripStage laterTripStage = null;
        boolean isFirstApplicableTripStage = true;

        for (int stageIndex = tripStages.size() - 1; stageIndex >= 0; stageIndex--) {
            evaluatedTripStage = tripStages.get(stageIndex);

            if (evaluatedTripStage.getTime() <= ticketShouldBeValidUntil) {
                if (isTicketValid(evaluatedTripStage)) {

                    if (isFirstApplicableTripStage) {
                        if (isTicketValid(laterTripStage)) {
                            /*
                             * At this point we have made sure, that we can depart from the stop at the beginning of
                             * the evaluated trip stage and continue our trip to the next stop using this ticket
                             * (eg. for cases like in TransitTicketTest::shouldReturn0MinutesValid(), where:
                             * currentTripStage.getTime() < ticketShouldBeValidUntil < laterTripStage.getTime())
                             */
                            totalMinutesWhenValid = ticketShouldBeValidUntil - evaluatedTripStage.getTime() + 1;
                        } else {
                            /*
                             * We cannot use the evaluated ticket for this trip stage because the ticket is only
                             * guaranteed to be valid for the first stop of the evaluated trip stage (departing from this
                             * stop and travelling further to the next one is not possible)
                             */
                            break;
                        }
                        isFirstApplicableTripStage = false;
                    } else {
                        totalMinutesWhenValid = ticketShouldBeValidUntil - evaluatedTripStage.getTime() + 1;
                    }

                } else {
                    break;
                }
            }
            laterTripStage = evaluatedTripStage;

        }
        return totalMinutesWhenValid;
    }

    private boolean isTicketValid(TransitTripStage tripStage) {
        if (Objects.isNull(tripStage)) return true;

        String agencyId = tripStage.getCurrentRoute().getId().getAgencyId();
        RoutePattern agencyAssociatedRoutePattern = routePatterns.get(agencyId);
        StopPattern agencyAssociatedStopPattern = stopPatterns.get(agencyId);

        if (Objects.isNull(agencyAssociatedRoutePattern) && Objects.isNull(agencyAssociatedStopPattern)) {
            return false;
        }

        boolean isTicketValid = true;
        if (Objects.nonNull(agencyAssociatedRoutePattern)) {
            isTicketValid &= agencyAssociatedRoutePattern.matches(tripStage.getCurrentRoute());
        }
        if (isTicketValid && Objects.nonNull(agencyAssociatedStopPattern)) {
            isTicketValid &= agencyAssociatedStopPattern.matches(tripStage.getCurrentStop());
        }

        return isTicketValid;
    }

    private int getMaxFaresConstraintCompliantTime(int ticketShouldBeValidUntil, List<TransitTripStage> tripStages) {
        int totalMinutesWhenValid = 0;

        int totalFareCount = 0;
        TransitTripStage evaluatedTripStage;
        TransitTripStage earlierTripStage = null;
        Route evaluatedRoute;
        Route nextRoute = null;

        for (int stageIndex = tripStages.size() - 1; stageIndex >= 0; stageIndex--) {

            evaluatedTripStage = tripStages.get(stageIndex);
            evaluatedRoute = evaluatedTripStage.getCurrentRoute();

            if (evaluatedTripStage.getTime() <= ticketShouldBeValidUntil) {

                if (isNull(earlierTripStage)) {
                    /*
                     * This is the first trip stage applicable to the part of the trip limited by
                     * ticketShouldBeValidUntil, so we are not concerned, whether the route associated with the
                     * next trip stage is different from the route associated with the evaluated stage or not - we
                     * are only counting this as the first fare for this ticket.
                     */
                    totalFareCount++;
                } else if (!nextRoute.getId().equals(evaluatedRoute.getId())) {
                    FareSwitch fareSwitch = new FareSwitch(evaluatedRoute, nextRoute, evaluatedTripStage.getCurrentStop(), earlierTripStage.getCurrentStop());
                    if (isFareSwitchValid(fareSwitch)) {
                        //We have made sure, that such fare switch is allowed for this ticket
                        totalFareCount++;

                        if (totalFareCount > maxFares) {
                            /*
                             * We have reached the limit of fare switches - we cannot use this ticket for another
                             * fare
                             */
                            break;
                        }
                    } else {
                        break;
                    }
                }
                totalMinutesWhenValid = ticketShouldBeValidUntil - evaluatedTripStage.getTime() + 1;

                nextRoute = evaluatedRoute;
                earlierTripStage = evaluatedTripStage;
            }
        }

        return totalMinutesWhenValid;
    }

    private boolean isFareSwitchValid(FareSwitch validatedFareSwitch) {
        return fareSwitchPatterns.isEmpty() ||
                fareSwitchPatterns.stream().anyMatch(fareSwitchPattern -> fareSwitchPattern.matches(validatedFareSwitch));
    }

    private int getMaxDistanceConstraintCompliantTime(int ticketShouldBeValidUntil, List<TransitTripStage> tripStages) {
        int totalMinutesWhenValid = 0;

        double totalDistance = 0;
        TransitTripStage evaluatedTripStage;
        TransitTripStage earlierTripStage;

        for (int stageIndex = tripStages.size() - 1; stageIndex > 0; stageIndex--) {

            evaluatedTripStage = tripStages.get(stageIndex);
            earlierTripStage = tripStages.get(stageIndex - 1);

            if (earlierTripStage.getTime() <= ticketShouldBeValidUntil) {
                /* I assume, that distance-limited tickets should be valid for the entire distance between stops
                 * (there is no ticket switching in the middle of the ride for means of transport that offer
                 * distance-limited tickets)
                 */
                totalDistance += evaluatedTripStage.getDistance();

                if (totalDistance <= maxDistance) {
                    if (notWalkingToSwitchFare(evaluatedTripStage)) {
                        totalMinutesWhenValid = ticketShouldBeValidUntil - earlierTripStage.getTime() + 1;
                    }
                } else {
                    break;
                }
            }
        }

        return totalMinutesWhenValid;
    }

    private boolean notWalkingToSwitchFare(TransitTripStage evaluatedTripStage) {
        return evaluatedTripStage.getDistance() > 0;
    }

}
