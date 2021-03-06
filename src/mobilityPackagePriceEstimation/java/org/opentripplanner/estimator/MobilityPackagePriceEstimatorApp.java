package org.opentripplanner.estimator;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MobilityPackagePriceEstimatorApp {

    private static final Logger LOG = LoggerFactory.getLogger(MobilityPackagePriceEstimatorApp.class);

    public static void main(String[] args) {
        EstimatorCommandLineParameters appParams = new EstimatorCommandLineParameters();
        try {
            new JCommander(appParams, args);
        } catch (ParameterException e) {
            LOG.error("Parameter error: {}", e.getMessage());
            System.exit(1);
        }

        MobilityPackagePriceEstimator priceEstimator = new MobilityPackagePriceEstimator(appParams);
        priceEstimator.estimatePrice(appParams.getRequestsPerSnapshot());

        System.exit(0);
    }

}
