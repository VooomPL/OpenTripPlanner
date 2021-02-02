package org.opentripplanner.pricing.transit.ticket.pattern;

import org.opentripplanner.model.Stop;

import java.util.ArrayList;
import java.util.HashMap;

public class StopPattern extends Pattern<Stop> {

    public enum StopAttribute {ID, NAME, ZONE, LATITUDE, LONGITUDE}

    private final HashMap<Pattern.TextOperator, ArrayList<String>> idConstraints = new HashMap<>();

    private final HashMap<Pattern.TextOperator, ArrayList<String>> nameConstraints = new HashMap<>();

    private final HashMap<Pattern.TextOperator, ArrayList<String>> zoneConstraints = new HashMap<>();

    private final HashMap<Pattern.NumericalOperator, Double> latitudeConstraints = new HashMap<>();

    private final HashMap<Pattern.NumericalOperator, Double> longitudeConstraints = new HashMap<>();

    //TODO: for rule-associated stop patterns-how to match with stop? private final RoutePattern routePattern = new RoutePattern();

    public void addConstraint(StopAttribute attribute, Pattern.TextOperator operator, String patternValue) {
        switch (attribute) {
            case ID:
                addConstraint(idConstraints, operator, patternValue);
                break;
            case NAME:
                addConstraint(nameConstraints, operator, patternValue);
                break;
            case ZONE:
                addConstraint(zoneConstraints, operator, patternValue);
                break;
            default:
                break;
        }
    }

    public void addConstraint(StopAttribute attribute, Pattern.NumericalOperator operator, Double patternValue) {
        switch (attribute) {
            case LATITUDE:
                latitudeConstraints.put(operator, patternValue);
                break;
            case LONGITUDE:
                longitudeConstraints.put(operator, patternValue);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean matches(Stop validatedObject) {
        return matches(idConstraints, validatedObject.getId().getId()) &&
                matches(nameConstraints, validatedObject.getName()) &&
                matches(zoneConstraints, validatedObject.getZoneId()) &&
                matches(latitudeConstraints, validatedObject.getLat()) &&
                matches(longitudeConstraints, validatedObject.getLon());
    }

}
