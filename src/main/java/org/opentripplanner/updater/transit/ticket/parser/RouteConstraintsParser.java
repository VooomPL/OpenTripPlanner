package org.opentripplanner.updater.transit.ticket.parser;

import org.opentripplanner.pricing.transit.ticket.pattern.Pattern;
import org.opentripplanner.pricing.transit.ticket.pattern.RoutePattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouteConstraintsParser {

    private static final Logger LOG = LoggerFactory.getLogger(RouteConstraintsParser.class);

    public static void parseConstraints(RoutePattern routePattern, String objectPropertyAsText, String operatorAsText, String patternAsText) {
        switch (objectPropertyAsText) {
            case "route_id":
                try {
                    Pattern.TextOperator operator = Pattern.TextOperator.valueOf(operatorAsText);
                    addConstraint(routePattern, RoutePattern.RouteAttribute.ID, operator, patternAsText);
                } catch (IllegalArgumentException e) {
                    LOG.warn("Unrecognized operator '{}' in ticket definition", operatorAsText);
                }
                break;
            case "route_short_name":
                try {
                    Pattern.TextOperator operator = Pattern.TextOperator.valueOf(operatorAsText);
                    addConstraint(routePattern, RoutePattern.RouteAttribute.SHORT_NAME, operator, patternAsText);
                } catch (IllegalArgumentException e) {
                    LOG.warn("Unrecognized operator '{}' in ticket definition", operatorAsText);
                }
                break;
            case "route_long_name":
                try {
                    Pattern.TextOperator operator = Pattern.TextOperator.valueOf(operatorAsText);
                    addConstraint(routePattern, RoutePattern.RouteAttribute.LONG_NAME, operator, patternAsText);
                } catch (IllegalArgumentException e) {
                    LOG.warn("Unrecognized operator '{}' in ticket definition", operatorAsText);
                }
                break;
            case "route_type":
                try {
                    Pattern.NumericalOperator operator = Pattern.NumericalOperator.valueOf(operatorAsText);
                    addConstraint(routePattern, RoutePattern.RouteAttribute.TYPE, operator, patternAsText);
                } catch (IllegalArgumentException e) {
                    LOG.warn("Unrecognized operator '{}' in ticket definition", operatorAsText);
                }
                break;
            default:
                LOG.warn("Unrecognized Stop property name '{}' in ticket definition", objectPropertyAsText);
        }
    }

    private static void addConstraint(RoutePattern routePattern, RoutePattern.RouteAttribute attribute,
                                      Pattern.TextOperator operator, String patternAsText) {
        if (operator.equals(Pattern.TextOperator.IN) || operator.equals(Pattern.TextOperator.NOT_IN)) {
            if (patternAsText.startsWith("{")) {
                for (String pattern : patternAsText.substring(1, patternAsText.length() - 1).split(",")) {
                    routePattern.addConstraint(attribute, operator, pattern.trim());
                }
            } else {
                LOG.warn("No opening brace in pattern '{}' for IN or NOT_IN operator in ticket definition", patternAsText);
            }
        } else {
            routePattern.addConstraint(attribute, operator, patternAsText);
        }
    }

    private static void addConstraint(RoutePattern routePattern, RoutePattern.RouteAttribute attribute,
                                      Pattern.NumericalOperator operator, String patternAsText) {
        try {
            double routeType = Double.parseDouble(patternAsText);
            routePattern.addConstraint(attribute, operator, routeType);
        } catch (NumberFormatException e) {
            LOG.warn("Pattern '{}' in ticket definition is not a number", patternAsText);
        }
    }

}
