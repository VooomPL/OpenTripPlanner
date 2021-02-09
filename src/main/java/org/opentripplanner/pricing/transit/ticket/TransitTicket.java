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

    @Getter
    private final RoutePattern routePattern = new RoutePattern();

    @Getter
    private final StopPattern stopPattern = new StopPattern();

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
        boolean isEvaluatedRouteValid, isEvaluatedStopValid;
        boolean isFirstApplicableTripStage = true;

        for (int stageIndex = tripStages.size() - 1; stageIndex >= 0; stageIndex--) {
            evaluatedTripStage = tripStages.get(stageIndex);

            isEvaluatedRouteValid = routePattern.matches(evaluatedTripStage.getCurrentRoute());
            isEvaluatedStopValid = stopPattern.matches(evaluatedTripStage.getCurrentStop());

            if (evaluatedTripStage.getTime() < ticketShouldBeValidUntil) {

                if (isEvaluatedRouteValid && isEvaluatedStopValid) {

                    if (isFirstApplicableTripStage && !(stageIndex == tripStages.size() - 1)) {
                        //For cases like in TransitTicketTest::shouldReturn0MinutesValid
                        if (routePattern.matches(laterTripStage.getCurrentRoute()) &&
                                stopPattern.matches(laterTripStage.getCurrentStop())) {
                            totalMinutesWhenValid = ticketShouldBeValidUntil - evaluatedTripStage.getTime() + 1;
                        } else {
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

    private int getMaxFaresConstraintCompliantTime(int ticketShouldBeValidUntil, List<TransitTripStage> tripStages) {
        int totalMinutesWhenValid = 0;

        int totalFareCount = 0;
        TransitTripStage evaluatedTripStage;
        TransitTripStage earlierTripStage = null;
        Route evaluatedRoute;
        Route earlierRoute = null;

        for (int stageIndex = tripStages.size() - 1; stageIndex >= 0; stageIndex--) {

            evaluatedTripStage = tripStages.get(stageIndex);
            evaluatedRoute = tripStages.get(stageIndex).getCurrentRoute();

            if (evaluatedTripStage.getTime() < ticketShouldBeValidUntil) {

                if (isNull(earlierTripStage)) {
                    totalFareCount++;
                } else if (!earlierRoute.getId().equals(evaluatedRoute.getId())) {
                    FareSwitch fareSwitch = new FareSwitch(evaluatedRoute, earlierRoute, evaluatedTripStage.getCurrentStop(), earlierTripStage.getCurrentStop());
                    if (isFareSwitchValid(fareSwitch)) {
                        totalFareCount++;

                        if (totalFareCount > maxFares) break;
                    } else {
                        break;
                    }
                }
                totalMinutesWhenValid = ticketShouldBeValidUntil - evaluatedTripStage.getTime() + 1;

                earlierRoute = evaluatedRoute;
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

        int totalDistance = 0;
        TransitTripStage evaluatedTripStage;
        TransitTripStage earlierTripStage;

        for (int stageIndex = tripStages.size() - 1; stageIndex > 0; stageIndex--) {

            evaluatedTripStage = tripStages.get(stageIndex);
            earlierTripStage = tripStages.get(stageIndex - 1);

            if (earlierTripStage.getTime() < ticketShouldBeValidUntil) {
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