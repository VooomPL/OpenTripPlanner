package org.opentripplanner.estimator;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import org.geotools.measure.Latitude;
import org.geotools.measure.Longitude;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class EstimatorCommandLineParameters {

    @Parameter(names = {"--routerName"}, required = true, description = "City name (lower case)")
    private String routerName;

    @Parameter(names = {"--officeLat"}, required = true, validateWith = CorrectLatitude.class, description = "Office latitude")
    private Double officeLat;

    @Parameter(names = {"--officeLon"}, required = true, validateWith = CorrectLongitude.class, description = "Office longitude")
    private Double officeLon;

    @Parameter(names = {"--radius"}, required = true, description = "Estimated euclidean distance from worker home to the office")
    private Double radius;

    @Parameter(names = {"--requestsPerScenario"}, required = true, validateWith = ValidPositiveInteger.class, description = "Number of requests sent for a single vehicle positions snapshot")
    private Integer requestsPerScenario;

    @Parameter(names = {"--startDate"}, required = true, converter = DateConverter.class, description = "First snapshot date")
    private LocalDate evaluationStartDate;

    @Parameter(names = {"--evaluationDaysTotal"}, required = true, validateWith = ValidPositiveInteger.class, description = "Number of analyzed days")
    private int evaluationDaysTotal;

    @Parameter(names = {"--morningHoursMin"}, required = true, converter = TimeConverter.class, description = "Morning snapshots start time")
    private LocalTime morningHoursMin;

    @Parameter(names = {"--eveningHoursMin"}, required = true, converter = TimeConverter.class, description = "Evening snapshot start time")
    private LocalTime eveningHoursMin;

    @Parameter(names = {"--morningHoursMax"}, required = true, converter = TimeConverter.class, description = "Morning snapshots end time")
    private LocalTime morningHoursMax;

    @Parameter(names = {"--eveningHoursMax"}, required = true, converter = TimeConverter.class, description = "Evening snapshot end time")
    private LocalTime eveningHoursMax;

    @Parameter(names = {"--snapshotInterval"}, required = true, validateWith = ValidPositiveInteger.class, description = "Interval for snapshots (in minutes)")
    private int snapshotIntervalInMinutes;

    @Parameter(names = {"--snapshotDatabaseURL"}, required = true, description = "URL from which we want to download snapshots")
    private String databaseURL;

    @Parameter(names = {"--snapshotDatabasePass"}, required = true, description = "URL from which we want to download snapshots")
    private String databasePassword;

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

    public LocalDate getEvaluationStartDate() {
        return evaluationStartDate;
    }

    public int getEvaluationDaysTotal() {
        return evaluationDaysTotal;
    }

    public LocalTime getMorningHoursMin() {
        return morningHoursMin;
    }

    public LocalTime getEveningHoursMin() {
        return eveningHoursMin;
    }

    public LocalTime getMorningHoursMax() {
        return morningHoursMax;
    }

    public LocalTime getEveningHoursMax() {
        return eveningHoursMax;
    }

    public int getSnapshotIntervalInMinutes() {
        return snapshotIntervalInMinutes;
    }

    public String getDatabaseURL() {
        return databaseURL;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    public static class CorrectLatitude implements IParameterValidator {
        @Override
        public void validate(String name, String value) throws ParameterException {
            String errorMessage;
            try {
                double latitude = Double.parseDouble(value);
                if (latitude < Latitude.MIN_VALUE || latitude > Latitude.MAX_VALUE) {
                    errorMessage = String.format("%s = %s is not a valid latitude value.", name, value);
                    throw new ParameterException(errorMessage);
                }
            } catch (NumberFormatException e) {
                errorMessage = String.format("%s = %s is not a valid double value.", name, value);
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
                    errorMessage = String.format("%s = %s is not a valid longitude value.", name, value);
                    throw new ParameterException(errorMessage);
                }
            } catch (NumberFormatException e) {
                errorMessage = String.format("%s = %s is not a valid double value.", name, value);
                throw new ParameterException(errorMessage);
            }
        }
    }

    public static class ValidPositiveInteger implements IParameterValidator {
        @Override
        public void validate(String name, String value) throws ParameterException {
            String errorMessage;
            try {
                int requestsPerScenario = Integer.parseInt(value);
                if (requestsPerScenario <= 0) {
                    errorMessage = String.format("%s = %s is not a valid positive integer value.", name, value);
                    throw new ParameterException(errorMessage);
                }
            } catch (NumberFormatException e) {
                errorMessage = String.format("%s = %s is not a valid integer value.", name, value);
                throw new ParameterException(errorMessage);
            }
        }
    }

    public static class DateConverter implements IStringConverter<LocalDate> {

        @Override
        public LocalDate convert(String value) {
            try {
                return LocalDate.parse(value);
            } catch (DateTimeParseException e) {
                throw new ParameterException(String.format("%s is not a valid date.", value));
            }
        }
    }

    public static class TimeConverter implements IStringConverter<LocalTime> {

        @Override
        public LocalTime convert(String value) {
            try {
                return LocalTime.parse(value);
            } catch (DateTimeParseException e) {
                e.printStackTrace();
                throw new ParameterException(String.format("%s is not a valid time.", value));
            }
        }
    }
}
