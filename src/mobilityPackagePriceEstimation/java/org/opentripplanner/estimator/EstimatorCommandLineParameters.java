package org.opentripplanner.estimator;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import org.geotools.measure.Latitude;
import org.geotools.measure.Longitude;

public class EstimatorCommandLineParameters {

    @Parameter(names = {"--routerName"}, required = true, description = "City name (lower case)")
    private String routerName;

    @Parameter(names = {"--officeLat"}, required = true, validateWith = CorrectLatitude.class, description = "Office latitude")
    private Double officeLat;

    @Parameter(names = {"--officeLon"}, required = true, validateWith = CorrectLongitude.class, description = "Office longitude")
    private Double officeLon;

    @Parameter(names = {"--radius"}, required = true, description = "Estimated euclidean distance from worker home to the office")
    private Double radius;

    @Parameter(names = {"--requestsPerScenario"}, required = true, validateWith = CorrectNumberOfRequestsPerScenario.class, description = "Number of requests sent for a single mock database snapshot")
    private Integer requestsPerScenario;

    public String getRouterName() {
        return routerName;
    }

    public Double getOfficeLat() {
        return officeLat;
    }

    public Double getOfficeLon() {
        return officeLon;
    }

    public Double getRadius() {
        return radius;
    }

    public Integer getRequestsPerScenario() {
        return requestsPerScenario;
    }

    public static class CorrectLatitude implements IParameterValidator {
        @Override
        public void validate(String name, String value) throws ParameterException {
            String errorMessage;
            try {
                double latitude = Double.parseDouble(value);
                if (latitude < Latitude.MIN_VALUE || latitude > Latitude.MAX_VALUE) {
                    errorMessage = String.format("%s = %s is not a correct latitude value.", name, value);
                    throw new ParameterException(errorMessage);
                }
            } catch (NumberFormatException e) {
                errorMessage = String.format("%s = %s is not a correct double value.", name, value);
                throw new ParameterException(errorMessage);
            }
        }
    }

    public static class CorrectLongitude implements IParameterValidator {
        @Override
        public void validate(String name, String value) throws ParameterException {
            String errorMessage;
            try {
                double longtitude = Double.parseDouble(value);
                if (longtitude < Longitude.MIN_VALUE || longtitude > Longitude.MAX_VALUE) {
                    errorMessage = String.format("%s = %s is not a correct longtitude value.", name, value);
                    throw new ParameterException(errorMessage);
                }
            } catch (NumberFormatException e) {
                errorMessage = String.format("%s = %s is not a correct double value.", name, value);
                throw new ParameterException(errorMessage);
            }
        }
    }

    public static class CorrectNumberOfRequestsPerScenario implements IParameterValidator {
        @Override
        public void validate(String name, String value) throws ParameterException {
            String errorMessage;
            try {
                int requestsPerScenario = Integer.parseInt(value);
                if (requestsPerScenario <= 0) {
                    errorMessage = String.format("%s = %s is not a correct number of requests per scenario.", name, value);
                    throw new ParameterException(errorMessage);
                }
            } catch (NumberFormatException e) {
                errorMessage = String.format("%s = %s is not a correct integer value.", name, value);
                throw new ParameterException(errorMessage);
            }
        }
    }
}
