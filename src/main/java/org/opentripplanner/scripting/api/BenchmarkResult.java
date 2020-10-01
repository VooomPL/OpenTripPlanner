package org.opentripplanner.scripting.api;

import java.io.Serializable;
import java.util.List;

public class BenchmarkResult implements Serializable {
    public List<Double> realRemainingWeight;

    public BenchmarkResult(List<Double> realRemainingWeight, List<Double> estimatedRemainingWeight) {
        this.realRemainingWeight = realRemainingWeight;
        this.estimatedRemainingWeight = estimatedRemainingWeight;
    }

    public List<Double> estimatedRemainingWeight;
}
