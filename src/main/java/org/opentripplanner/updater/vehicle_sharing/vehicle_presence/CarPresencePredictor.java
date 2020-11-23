package org.opentripplanner.updater.vehicle_sharing.vehicle_presence;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.strtree.STRtree;
import org.opentripplanner.prediction_client.VehiclePresence;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleDescription;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleType;

import javax.annotation.Nullable;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;

public class CarPresencePredictor {

    private static final Duration SEVEN_AND_HALF = Duration.of(7, MINUTES).plus(Duration.of(30, SECONDS));
    private static final Duration FIFTEEN = Duration.of(15, MINUTES);
    private static final Duration THIRTY = Duration.of(30, MINUTES);
    private final STRtree index15;
    private final STRtree index30;
    private final STRtree index45;
    private final double width;
    private final double length;
    private final Instant indexThreshold15;
    private final Instant indexThreshold30;
    private final Instant indexThreshold45;

    public CarPresencePredictor(VehiclePresence vehiclePresenceHeatmaps) {
        index15 = new STRtree();
        for (VehiclePresence.Prediction prediction : vehiclePresenceHeatmaps.getPredictions_15()) {
            index15.insert(prediction.getEnvelope(), prediction);
        }
        index30 = new STRtree();
        for (VehiclePresence.Prediction prediction : vehiclePresenceHeatmaps.getPredictions_30()) {
            index30.insert(prediction.getEnvelope(), prediction);
        }
        index45 = new STRtree();
        for (VehiclePresence.Prediction prediction : vehiclePresenceHeatmaps.getPredictions_45()) {
            index45.insert(prediction.getEnvelope(), prediction);
        }
        index15.build();
        index30.build();
        index45.build();
        this.length = vehiclePresenceHeatmaps.getCellLength();
        this.width = vehiclePresenceHeatmaps.getCellWidth();

        Instant heatmapCreationTime = Instant.ofEpochSecond(vehiclePresenceHeatmaps.getTimestamp());
        this.indexThreshold15 = heatmapCreationTime.plus(SEVEN_AND_HALF);
        this.indexThreshold30 = heatmapCreationTime.plus(SEVEN_AND_HALF).plus(FIFTEEN);
        this.indexThreshold45 = heatmapCreationTime.plus(SEVEN_AND_HALF).plus(THIRTY);
    }

    /**
     * Calculate prediction value for vehicle presence in given position. vehiclePresencePredictor keeps cells with
     * positive probability of presence in future so if no cell is found it assumes that vehicle disappears and returns 0.
     *
     * @param vehicleDescription vehicle which we want to make prediction about
     * @param time               timestamp in seconds since epoch of point in time when we want to make prediction.
     *                           Used for choosing time braked from [0min, 15min, 30min, 45min] in the future.
     * @return probability value in range [0,1] indicating presence of vehicle in given time.
     */
    public double predict(VehicleDescription vehicleDescription, long time) {
        if (VehicleType.CAR.equals(vehicleDescription.getVehicleType())) {
            STRtree tree = chooseIndex(time);
            if (tree == null) {
                return 1;
            } else {
                return findGridCellValue(vehicleDescription.getLongitude(), vehicleDescription.getLatitude(), tree);
            }
        } else {
            return 1;
        }
    }

    private double findGridCellValue(double longitude, double latitude, STRtree tree) {
        List<VehiclePresence.Prediction> predictions = tree.query(new Envelope(longitude - width, longitude + width, latitude - length, latitude + length));
        for (VehiclePresence.Prediction prediction : predictions) {
            if (prediction.getEnvelope().contains(longitude, latitude)) {
                return prediction.getValue();
            }
        }
        return 0;
    }

    @Nullable
    private STRtree chooseIndex(long time) {
        Instant pointInTime = Instant.ofEpochSecond(time);
        if (indexThreshold15.isAfter(pointInTime)) {
            return null;
        } else if (indexThreshold30.isAfter(pointInTime)) {
            return index15;
        } else if (indexThreshold45.isAfter(pointInTime)) {
            return index30;
        } else {
            return index45;
        }
    }
}
