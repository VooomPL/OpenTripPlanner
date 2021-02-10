package org.opentripplanner.pricing.transit.ticket.pattern;

import org.opentripplanner.model.Route;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class RoutePattern extends Pattern<Route> {

    public enum RouteAttribute {ID, SHORT_NAME, LONG_NAME, TYPE, AGENCY_NAME}

    private final HashMap<Pattern.TextOperator, ArrayList<String>> idConstraints = new HashMap<>();
    private final HashMap<Pattern.TextOperator, ArrayList<String>> shortNameConstraints = new HashMap<>();
    private final HashMap<Pattern.TextOperator, ArrayList<String>> longNameConstraints = new HashMap<>();
    private final HashMap<Pattern.NumericalOperator, Double> typeConstraints = new HashMap<>();
    private final String agencyNameConstraint;

    public RoutePattern() {
        this(null);
    }

    public RoutePattern(String agencyName) {
        this.agencyNameConstraint = agencyName;
    }

    public void addConstraint(RouteAttribute attribute, Pattern.TextOperator operator, String patternValue) {
        switch (attribute) {
            case ID:
                addConstraint(idConstraints, operator, patternValue);
                break;
            case SHORT_NAME:
                addConstraint(shortNameConstraints, operator, patternValue);
                break;
            case LONG_NAME:
                addConstraint(longNameConstraints, operator, patternValue);
                break;
            default:
                break;
        }
    }

    public void addConstraint(RouteAttribute attribute, Pattern.NumericalOperator operator, Double patternValue) {
        switch (attribute) {
            case TYPE:
                typeConstraints.put(operator, patternValue);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean matches(Route validatedObject) {
        return Pattern.matches(idConstraints, validatedObject.getId().getId()) &&
                Pattern.matches(shortNameConstraints, validatedObject.getShortName()) &&
                Pattern.matches(longNameConstraints, validatedObject.getLongName()) &&
                Pattern.matches(typeConstraints, Double.valueOf(validatedObject.getType())) &&
                (Objects.isNull(agencyNameConstraint) || agencyNameConstraint.equals(validatedObject.getAgency().getName()));
    }

}
