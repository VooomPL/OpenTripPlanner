package org.opentripplanner.updater.transit.ticket.parser;

import org.opentripplanner.pricing.transit.ticket.pattern.Pattern;
import org.opentripplanner.pricing.transit.ticket.pattern.StopPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StopConstraintsParser {

    private static final Logger LOG = LoggerFactory.getLogger(StopConstraintsParser.class);

    public static void parseConstraints(StopPattern stopPattern, String objectPropertyAsText, String operatorAsText, String patternAsText) {
        switch (objectPropertyAsText) {
            case "stop_id":
                try {
                    Pattern.TextOperator operator = Pattern.TextOperator.valueOf(operatorAsText);
                    addConstraint(stopPattern, StopPattern.StopAttribute.ID, operator, patternAsText);
                } catch (IllegalArgumentException e) {
                    LOG.error("Unrecognized operator '{}' in ticket definition", operatorAsText);
                }
                break;
            case "stop_name":
                try {
                    Pattern.TextOperator operator = Pattern.TextOperator.valueOf(operatorAsText);
                    addConstraint(stopPattern, StopPattern.StopAttribute.NAME, operator, patternAsText);
                } catch (IllegalArgumentException e) {
                    LOG.error("Unrecognized operator '{}' in ticket definition", operatorAsText);
                }
                break;
            case "stop_lat":
                try {
                    Pattern.NumericalOperator operator = Pattern.NumericalOperator.valueOf(operatorAsText);
                    addConstraint(stopPattern, StopPattern.StopAttribute.LATITUDE, operator, patternAsText);
                } catch (IllegalArgumentException e) {
                    LOG.error("Unrecognized operator '{}' in ticket definition", operatorAsText);
                }
                break;
            case "stop_lon":
                try {
                    Pattern.NumericalOperator operator = Pattern.NumericalOperator.valueOf(operatorAsText);
                    addConstraint(stopPattern, StopPattern.StopAttribute.LONGITUDE, operator, patternAsText);
                } catch (IllegalArgumentException e) {
                    LOG.error("Unrecognized operator '{}' in ticket definition", operatorAsText);
                }
                break;
            case "zone_id":
                try {
                    Pattern.TextOperator operator = Pattern.TextOperator.valueOf(operatorAsText);
                    addConstraint(stopPattern, StopPattern.StopAttribute.ZONE, operator, patternAsText);
                } catch (IllegalArgumentException e) {
                    LOG.error("Unrecognized operator '{}' in ticket definition", operatorAsText);
                }
                break;
            default:
                LOG.error("Unrecognized Stop property name '{}' in ticket definition", objectPropertyAsText);
        }
    }

    private static void addConstraint(StopPattern stopPattern, StopPattern.StopAttribute attribute,
                                      Pattern.TextOperator operator, String patternAsText) {
        if (operator.equals(Pattern.TextOperator.IN) || operator.equals(Pattern.TextOperator.NOT_IN)) {
            if (patternAsText.startsWith("{")) {
                for (String pattern : patternAsText.substring(1, patternAsText.length() - 1).split(",")) {
                    stopPattern.addConstraint(attribute, operator, pattern.trim());
                }
            } else {
                LOG.error("No opening brace in pattern '{}' for IN or NOT_IN operator in ticket definition", patternAsText);
            }
        } else {
            stopPattern.addConstraint(attribute, operator, patternAsText);
        }
    }

    private static void addConstraint(StopPattern stopPattern, StopPattern.StopAttribute attribute,
                                      Pattern.NumericalOperator operator, String patternAsText) {
        try {
            double routeType = Double.parseDouble(patternAsText);
            stopPattern.addConstraint(attribute, operator, routeType);
        } catch (NumberFormatException e) {
            LOG.warn("Pattern '{}' in ticket definition is not a number", patternAsText);
        }
    }

}
