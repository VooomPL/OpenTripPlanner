package org.opentripplanner.updater.vehicle_sharing.vehicle_presence;

import junit.framework.TestCase;
import org.locationtech.jts.geom.Envelope;
import org.opentripplanner.hasura_client.hasura_objects.VehiclePresence;
import org.opentripplanner.routing.core.vehicle_sharing.CarDescription;
import org.opentripplanner.routing.core.vehicle_sharing.FuelType;
import org.opentripplanner.routing.core.vehicle_sharing.Gearbox;

import java.util.Collections;
import java.util.List;

public class CarPresencePredictorTest extends TestCase {

    private CarPresencePredictor carPresencePredictor = new CarPresencePredictor();
    private static long MINUTE = 60;

    private VehiclePresence prepareHeatMap() {
        VehiclePresence vehiclePresence = new VehiclePresence();
        vehiclePresence.setVehicle("car");
        vehiclePresence.setTime(0);
        vehiclePresence.setCellLength(0.001);
        vehiclePresence.setCellWidth(0.001);

        return vehiclePresence;
    }

    private VehiclePresence.Prediction createPrediction(double lon, double lat, double val) {
        VehiclePresence.Prediction prediction = new VehiclePresence.Prediction(lon, lat, val, new Envelope(lon, lon + 0.001, lat, lat + 0.001));
        return prediction;
    }


    public void testTimeIntervalChoosingLogic() {
        // given
        double[] values = {0.9, 0.7, 0.5};
        long[] times = {10, MINUTE * 8, MINUTE * 20, MINUTE * 25, MINUTE * 31, MINUTE * 40, MINUTE * 50, MINUTE * 500};
        double[] expected = {1, 0.9, 0.9, 0.7, 0.7, 0.5, 0.5, 0.5};

        VehiclePresence vehiclePresence = prepareHeatMap();

        vehiclePresence.setPredictions_15(List.of(new VehiclePresence.Prediction(1, 1, values[0], new Envelope(1, 1.001, 1, 1.001))));
        vehiclePresence.setPredictions_30(List.of(new VehiclePresence.Prediction(1, 1, values[1], new Envelope(1, 1.001, 1, 1.001))));
        vehiclePresence.setPredictions_45(List.of(new VehiclePresence.Prediction(1, 1, values[2], new Envelope(1, 1.001, 1, 1.001))));

        carPresencePredictor.updateVehiclePresenceHeatmap(vehiclePresence);
        CarDescription carDescription = new CarDescription("id", 1.0005, 1.0005, FuelType.HYBRID, Gearbox.AUTOMATIC, 0, "innogy", 100000.0);

        //when
        for (int i = 0; i < 8; i++) {
            double result = carPresencePredictor.predict(carDescription, times[i]);

            // then
            assertEquals(expected[i], result);
        }
    }

    public void testPredict() {
        // given
        VehiclePresence vehiclePresence = prepareHeatMap();
        vehiclePresence.setPredictions_15(List.of(
                new VehiclePresence.Prediction(1, 1, 0.1, new Envelope(1, 1.001, 1, 1.001)),
                new VehiclePresence.Prediction(1.001, 1, 0.2, new Envelope(1.001, 1.002, 1, 1.001)),
                new VehiclePresence.Prediction(1, 1.001, 0.3, new Envelope(1, 1.001, 1.001, 1.002)),
                new VehiclePresence.Prediction(1.001, 1.001, 0.4, new Envelope(1.001, 1.002, 1.001, 1.002)),
                new VehiclePresence.Prediction(0.999, 0.999, 0.5, new Envelope(0.999, 1, 0.999, 1)),
                new VehiclePresence.Prediction(0.999, 1, 0.6, new Envelope(0.999, 1, 1, 1.001)),
                new VehiclePresence.Prediction(1, 0.999, 0.7, new Envelope(1, 1.001, 0.999, 1)),
                new VehiclePresence.Prediction(0.999, 1.001, 0.8, new Envelope(0.999, 1, 1.001, 1.002)),
                new VehiclePresence.Prediction(1.001, 0.999, 0.9, new Envelope(1.001, 1.002, 0.999, 1))
        ));
        vehiclePresence.setPredictions_30(Collections.emptyList());
        vehiclePresence.setPredictions_45(Collections.emptyList());
        carPresencePredictor.updateVehiclePresenceHeatmap(vehiclePresence);

        CarDescription[] carDescription = {
                new CarDescription("id", 1.0001, 1.0005, FuelType.HYBRID, Gearbox.AUTOMATIC, 0, "innogy", 100000.0),
                new CarDescription("id", 0.9995, 1.0005, FuelType.HYBRID, Gearbox.AUTOMATIC, 0, "innogy", 100000.0),
                new CarDescription("id", 1.0011, 0.9999, FuelType.HYBRID, Gearbox.AUTOMATIC, 0, "innogy", 100000.0),
                new CarDescription("id", 1.0011, 1.0011, FuelType.HYBRID, Gearbox.AUTOMATIC, 0, "innogy", 100000.0)
        };
        double[] expected = {0.1, 0.6, 0.9, 0.4};

        // when
        for(int i=0; i< 4; i++) {
            double result = carPresencePredictor.predict(carDescription[i], 10 * MINUTE);

            // then
            assertEquals(expected[i], result);
        }

    }


}