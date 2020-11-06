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
    private long time;
    private STRtree index15;
    private STRtree index30;
    private STRtree index45;
    private double width;
    private double length;

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
        if (VehicleType.CAR.name().equalsIgnoreCase(vehicleDescription.getVehicleType().name())) {
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

    public void updateVehiclePresenceHeatmap(VehiclePresence vehiclePresenceHeatmaps) {
        if (vehiclePresenceHeatmaps.getVehicle().equalsIgnoreCase(VehicleType.CAR.name())) {
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
            this.time = vehiclePresenceHeatmaps.getTimestamp();
            this.length = vehiclePresenceHeatmaps.getCellLength();
            this.width = vehiclePresenceHeatmaps.getCellWidth();
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
        Instant heatmapCreationTime = Instant.ofEpochSecond(this.time);
        if (heatmapCreationTime.plus(SEVEN_AND_HALF).isAfter(pointInTime)) {
            return null;
        } else if (heatmapCreationTime.plus(FIFTEEN).plus(SEVEN_AND_HALF).isAfter(pointInTime)) {
            return index15;
        } else if (heatmapCreationTime.plus(THIRTY).plus(SEVEN_AND_HALF).isAfter(pointInTime)) {
            return index30;
        } else {
            return index45;
        }
    }
}
