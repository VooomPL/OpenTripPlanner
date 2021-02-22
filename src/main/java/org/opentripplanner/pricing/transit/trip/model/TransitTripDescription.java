package org.opentripplanner.pricing.transit.trip.model;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import lombok.Getter;
import org.opentripplanner.model.Route;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class TransitTripDescription {

    @Getter
    private final List<TransitTripStage> tripStages = new ArrayList<>();

    private final RangeSet<Integer> tripStagesTimeBounds = TreeRangeSet.create();

    public TransitTripDescription(List<TransitTripStage> tripStages) {
        if (Objects.nonNull(tripStages) && !tripStages.isEmpty()) {

            TransitTripStage currentTripStage;
            Route currentRoute = tripStages.get(0).getCurrentRoute();
            int currentRangeFirstMinute = tripStages.get(0).getTime();
            int currentRangeLastMinute = currentRangeFirstMinute;

            for (int stageIndex = 0; stageIndex < tripStages.size(); stageIndex++) {
                currentTripStage = tripStages.get(stageIndex);
                this.tripStages.add(currentTripStage);
                if (!currentRoute.getId().getId().equals(currentTripStage.getCurrentRoute().getId().getId())) {
                    this.tripStagesTimeBounds.add(Range.closed(currentRangeFirstMinute, currentRangeLastMinute));
                    currentRangeFirstMinute = currentTripStage.getTime();
                } else if (stageIndex == tripStages.size() - 1) {
                    currentRangeLastMinute = currentTripStage.getTime();
                    this.tripStagesTimeBounds.add(Range.closed(currentRangeFirstMinute, currentRangeLastMinute));
                }
                currentRoute = currentTripStage.getCurrentRoute();
                currentRangeLastMinute = currentTripStage.getTime();
            }
        }
    }

    public boolean isEmpty() {
        return tripStages.isEmpty();
    }

    public boolean isTravelingAtMinute(int minute) {
        return tripStagesTimeBounds.contains(minute);
    }

    public int getLastMinute() {
        return tripStages.get(tripStages.size() - 1).getTime();
    }

    public int getLastMinuteOfPreviousFare(int currentMinuteOfTravel) {
        Iterator<Range<Integer>> rangeIterator = tripStagesTimeBounds.asRanges().iterator();
        Range<Integer> evaluatedRange;
        int result = 0;
        while (rangeIterator.hasNext()) {
            evaluatedRange = rangeIterator.next();
            if (evaluatedRange.upperEndpoint() < currentMinuteOfTravel) {
                result = evaluatedRange.upperEndpoint();
                break;
            }
        }
        return result;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append("[");
        for (TransitTripStage tripStage : tripStages) {
            result.append(tripStage);
        }
        result.append("]");

        return result.toString();
    }
}
