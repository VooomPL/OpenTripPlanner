package org.opentripplanner.pricing.ticket.pattern;

import org.opentripplanner.model.Route;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class RoutePattern {

    public enum RouteAttribute {ID, SHORT_NAME, LONG_NAME, TYPE}

    private HashMap<Pattern.TextOperator, ArrayList<String>> idConstraints = new HashMap<>();
    private HashMap<Pattern.TextOperator, ArrayList<String>> shortNameConstraints = new HashMap<>();
    private HashMap<Pattern.TextOperator, ArrayList<String>> longNameConstraints = new HashMap<>();
    private HashMap<Pattern.NumericalOperator, Double> typeConstraints = new HashMap<>();

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

    private void addConstraint(HashMap<Pattern.TextOperator, ArrayList<String>> constraintMap, Pattern.TextOperator operator, String patternValue) {
        if (constraintMap.containsKey(operator)) {
            constraintMap.get(operator).add(patternValue);
        } else {
            constraintMap.put(operator, new ArrayList<>(Collections.singletonList(patternValue)));
        }
    }

    public boolean matches(Route route) {
        return Pattern.matches(idConstraints, route.getId().getId()) &&
                Pattern.matches(shortNameConstraints, route.getShortName()) &&
                Pattern.matches(longNameConstraints, route.getLongName()) &&
                Pattern.matches(typeConstraints, Double.valueOf(route.getType()));
    }

}
