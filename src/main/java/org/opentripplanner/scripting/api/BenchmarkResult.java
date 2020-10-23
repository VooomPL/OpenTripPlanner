package org.opentripplanner.scripting.api;

import java.io.Serializable;
import java.util.List;

public class BenchmarkResult implements Serializable {
    public List<Double> realRemainingWeight;
    public List<Double> estimatedRemainingWeight;
    public List<Long> time;

    public BenchmarkResult(List<Double> realRemainingWeight, List<Double> estimatedRemainingWeight, List<Long> time) {
        this.realRemainingWeight = realRemainingWeight;
        this.estimatedRemainingWeight = estimatedRemainingWeight;
        this.time = time;
    }

}
