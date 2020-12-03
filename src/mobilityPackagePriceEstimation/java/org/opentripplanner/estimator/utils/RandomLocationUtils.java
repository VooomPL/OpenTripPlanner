package org.opentripplanner.estimator.utils;

import org.geotools.measure.Latitude;
import org.geotools.measure.Longitude;
import org.opentripplanner.common.model.GenericLocation;

import java.util.Random;

public class RandomLocationUtils {

    private static final Random random = new Random();

    public static GenericLocation generateRandomLocation(GenericLocation center, double radius) {
        double r = radius * Math.sqrt(random.nextDouble());
        double theta = random.nextDouble() * 2 * Math.PI;

        double latitude = fixCoordinate(center.lat + r * Math.sin(theta), Latitude.MIN_VALUE, Latitude.MAX_VALUE);
        double longitude = fixCoordinate(center.lng + r * Math.cos(theta), Longitude.MIN_VALUE, Longitude.MAX_VALUE);

        return new GenericLocation(latitude, longitude);
    }

    public static double fixCoordinate(double originalValue, double minValue, double maxValue) {
        double result;

        if (originalValue < minValue) {
            result = maxValue + (originalValue - minValue);
        } else if (originalValue > maxValue) {
            result = minValue + (originalValue - maxValue);
        } else {
            result = originalValue;
        }

        return result;
    }
}
