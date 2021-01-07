package org.opentripplanner.pricing.ticket.pattern;

import org.opentripplanner.model.Stop;

import java.util.ArrayList;
import java.util.HashMap;

public class StopPattern extends Pattern<Stop> {

    public enum StopAttribute {ID, NAME, ZONE, LATITUDE, LONGITUDE}

    private HashMap<Pattern.TextOperator, ArrayList<String>> idConstraints;
    private HashMap<Pattern.TextOperator, ArrayList<String>> nameConstraints;
    private HashMap<Pattern.TextOperator, ArrayList<String>> zoneConstraints;
    private HashMap<Pattern.NumericalOperator, Double> latitudeConstraints;
    private HashMap<Pattern.NumericalOperator, Double> longitudeConstraints;
    //TODO: RoutePattern - może być nullem - wtedy reguła obowiązuje niezależnie od linii
    //TODO: Testy!!!

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
        return Pattern.matches(idConstraints, validatedObject.getId().getId()) &&
                Pattern.matches(nameConstraints, validatedObject.getName()) &&
                Pattern.matches(zoneConstraints, validatedObject.getZoneId()) &&
                Pattern.matches(latitudeConstraints, validatedObject.getLat()) &&
                Pattern.matches(longitudeConstraints, validatedObject.getLon());
    }

}
