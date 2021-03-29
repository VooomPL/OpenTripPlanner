package org.opentripplanner.estimator.utils;

import org.geotools.measure.Latitude;
import org.geotools.measure.Longitude;
import org.opentripplanner.common.model.GenericLocation;

import java.util.Random;

public class RandomLocationGenerator {

    private Random random;

    public RandomLocationGenerator(Random random) {
        this.random = random;
    }

    public GenericLocation generateRandomLocation(GenericLocation center, double radius) {
        double r = radius * Math.sqrt(random.nextDouble());
        double theta = random.nextDouble() * 2 * Math.PI;

        double longitude = fixCoordinate(center.lng + r * Math.cos(theta), Longitude.MIN_VALUE, Longitude.MAX_VALUE);
        double latitude = fixCoordinate(center.lat + r * Math.sin(theta), Latitude.MIN_VALUE, Latitude.MAX_VALUE);

        return new GenericLocation(latitude, longitude);
    }

    private double fixCoordinate(double originalValue, double minValue, double maxValue) {
        double result;

        double diff = maxValue - minValue;

        if (originalValue < minValue) {
            result = originalValue + diff;
        } else if (originalValue > maxValue) {
            result = originalValue - diff;
        } else {
            result = originalValue;
        }

        return result;
    }
}
