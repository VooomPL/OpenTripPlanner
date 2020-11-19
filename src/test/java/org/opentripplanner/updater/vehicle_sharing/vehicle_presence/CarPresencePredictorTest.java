package org.opentripplanner.updater.vehicle_sharing.vehicle_presence;

import org.junit.Test;
import org.locationtech.jts.geom.Envelope;
import org.opentripplanner.prediction_client.VehiclePresence;
import org.opentripplanner.routing.core.vehicle_sharing.CarDescription;
import org.opentripplanner.routing.core.vehicle_sharing.FuelType;
import org.opentripplanner.routing.core.vehicle_sharing.Gearbox;
import org.opentripplanner.routing.core.vehicle_sharing.VehicleType;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CarPresencePredictorTest {

    private static long MINUTE = 60;

    private VehiclePresence prepareHeatMap() {
        VehiclePresence vehiclePresence = new VehiclePresence();
        vehiclePresence.setVehicleType(VehicleType.CAR.name());
        vehiclePresence.setTimestamp(0);
        vehiclePresence.setCellLength(0.001);
        vehiclePresence.setCellWidth(0.001);

        return vehiclePresence;
    }

    private VehiclePresence.Prediction createPrediction(double lon, double lat, double val) {
        VehiclePresence.Prediction prediction = new VehiclePresence.Prediction(lon, lat, val, new Envelope(lon, lon + 0.001, lat, lat + 0.001));
        return prediction;
    }

    @Test
    public void testTimeIntervalChoosingLogic() {
        // given
        long[] times = {10, MINUTE * 8, MINUTE * 20, MINUTE * 25, MINUTE * 31, MINUTE * 40, MINUTE * 50, MINUTE * 500};
        double[] expected = {1, 0.9, 0.9, 0.7, 0.7, 0.5, 0.5, 0.5};

        VehiclePresence vehiclePresence = prepareHeatMap();

        vehiclePresence.setPredictions_15(List.of(createPrediction(1, 1, 0.9)));
        vehiclePresence.setPredictions_30(List.of(createPrediction(1, 1, 0.7)));
        vehiclePresence.setPredictions_45(List.of(createPrediction(1, 1, 0.5)));
        CarPresencePredictor carPresencePredictor = new CarPresencePredictor(vehiclePresence);

        CarDescription carDescription = new CarDescription("id", 1.0005, 1.0005, FuelType.HYBRID, Gearbox.AUTOMATIC, 0, "innogy", 100000.0);

        //when
        for (int i = 0; i < 8; i++) {
            double result = carPresencePredictor.predict(carDescription, times[i]);

            // then
            assertEquals(expected[i], result, 0.0001);
        }
    }

    @Test
    public void testPredict() {
        // given
        VehiclePresence vehiclePresence = prepareHeatMap();
        vehiclePresence.setPredictions_15(List.of(
                createPrediction(1,1,0.1),
                createPrediction(1.001,1,0.2),
                createPrediction(1,1.001,0.3),
                createPrediction(1.001,1.001,0.4),
                createPrediction(0.999,0.999,0.5),
                createPrediction(0.999,1,0.6),
                createPrediction(1,0.999,0.7),
                createPrediction(0.999,1.001,0.8),
                createPrediction(1.001,0.999,0.9)
        ));
        vehiclePresence.setPredictions_30(Collections.emptyList());
        vehiclePresence.setPredictions_45(Collections.emptyList());
        CarPresencePredictor carPresencePredictor = new CarPresencePredictor(vehiclePresence);

        CarDescription[] carDescription = {
                new CarDescription("id", 1.0001, 1.0005, FuelType.HYBRID, Gearbox.AUTOMATIC, 0, "innogy", 100000.0),
                new CarDescription("id", 0.9995, 1.0005, FuelType.HYBRID, Gearbox.AUTOMATIC, 0, "innogy", 100000.0),
                new CarDescription("id", 1.0011, 0.9999, FuelType.HYBRID, Gearbox.AUTOMATIC, 0, "innogy", 100000.0),
                new CarDescription("id", 1.0011, 1.0011, FuelType.HYBRID, Gearbox.AUTOMATIC, 0, "innogy", 100000.0)
        };
        double[] expected = {0.1, 0.6, 0.9, 0.4};

        // when
        for (int i = 0; i < 4; i++) {
            double result = carPresencePredictor.predict(carDescription[i], 10 * MINUTE);

            // then
            assertEquals(expected[i], result, 0.0001);
        }

    }


}