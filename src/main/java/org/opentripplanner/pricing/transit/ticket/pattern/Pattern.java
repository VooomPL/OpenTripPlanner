package org.opentripplanner.pricing.transit.ticket.pattern;

import java.util.*;

public abstract class Pattern<T> {

    public enum TextOperator {STARTS_WITH, ENDS_WITH, IN, NOT_STARTS_WITH, NOT_ENDS_WITH, NOT_IN}

    public enum NumericalOperator {GREATER_THAN, LESS_THAN, GREATER_OR_EQUAL, LESS_OR_EQUAL, EQUAL, NOT_EQUAL}

    protected static boolean matches(TextOperator operator, String testedValue, List<String> patternValues) {
        if (Objects.isNull(testedValue)) {
            return false;
        } else {
            switch (operator) {
                case STARTS_WITH:
                    for (String patternValue : patternValues) {
                        if (testedValue.startsWith(patternValue)) {
                            return true;
                        }
                    }
                    return false;
                case ENDS_WITH:
                    for (String patternValue : patternValues) {
                        if (testedValue.endsWith(patternValue)) {
                            return true;
                        }
                    }
                    return false;
                case IN:
                    return patternValues.contains(testedValue);
                case NOT_STARTS_WITH:
                    for (String patternValue : patternValues) {
                        if (testedValue.startsWith(patternValue)) {
                            return false;
                        }
                    }
                    return true;
                case NOT_ENDS_WITH:
                    for (String patternValue : patternValues) {
                        if (testedValue.endsWith(patternValue)) {
                            return false;
                        }
                    }
                    return true;
                case NOT_IN:
                    return !patternValues.contains(testedValue);
                default:
                    return false;
            }
        }
    }

    protected static boolean matches(NumericalOperator operator, Double testedValue, Double patternValue) {
        if (Objects.isNull(testedValue)) {
            return false;
        } else {
            switch (operator) {
                case GREATER_THAN:
                    return testedValue.compareTo(patternValue) > 0;
                case LESS_THAN:
                    return testedValue.compareTo(patternValue) < 0;
                case GREATER_OR_EQUAL:
                    return testedValue.compareTo(patternValue) >= 0;
                case LESS_OR_EQUAL:
                    return testedValue.compareTo(patternValue) <= 0;
                case EQUAL:
                    return testedValue.equals(patternValue);
                case NOT_EQUAL:
                    return !testedValue.equals(patternValue);
                default:
                    return false;
            }
        }
    }

    protected void addConstraint(HashMap<TextOperator, ArrayList<String>> constraintMap, TextOperator operator, String patternValue) {
        if (constraintMap.containsKey(operator)) {
            constraintMap.get(operator).add(patternValue);
        } else {
            constraintMap.put(operator, new ArrayList<>(Collections.singletonList(patternValue)));
        }
    }

    protected static boolean matches(HashMap<TextOperator, ArrayList<String>> constraints, String testedValue) {
        for (TextOperator operator : constraints.keySet()) {
            if (!matches(operator, testedValue, constraints.get(operator))) {
                return false;
            }
        }
        return true;
    }

    protected static boolean matches(HashMap<NumericalOperator, Double> constraints, Double testedValue) {
        for (NumericalOperator operator : constraints.keySet()) {
            if (!matches(operator, testedValue, constraints.get(operator))) {
                return false;
            }
        }
        return true;
    }

    public abstract boolean matches(T validatedObject);
}
