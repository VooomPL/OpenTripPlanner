package org.opentripplanner.pricing.transit.ticket.pattern;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PatternTest {

    @Test
    public void shouldMatchStartsWithStringLiteral() {
        ArrayList<String> patternValues = new ArrayList<>();
        patternValues.add("L");

        assertTrue(Pattern.matches(Pattern.TextOperator.STARTS_WITH, "L10", patternValues));
    }

    @Test
    public void shouldNotMatchStartsWithStringLiteral() {
        ArrayList<String> patternValues = new ArrayList<>();
        patternValues.add("L");

        assertFalse(Pattern.matches(Pattern.TextOperator.STARTS_WITH, "D10", patternValues));
    }

    @Test
    public void shouldMatchStartsWithMultipleAlternatives() {
        ArrayList<String> patternValues = new ArrayList<>();
        patternValues.add("A");
        patternValues.add("C");

        assertTrue(Pattern.matches(Pattern.TextOperator.STARTS_WITH, "A10", patternValues));
        assertFalse(Pattern.matches(Pattern.TextOperator.STARTS_WITH, "B10", patternValues));
        assertTrue(Pattern.matches(Pattern.TextOperator.STARTS_WITH, "C10", patternValues));
    }

    @Test
    public void shouldMatchNotStartsWithStringLiteral() {
        ArrayList<String> patternValues = new ArrayList<>();
        patternValues.add("L");

        assertTrue(Pattern.matches(Pattern.TextOperator.NOT_STARTS_WITH, "10N", patternValues));
    }

    @Test
    public void shouldNotMatchNotStartsWithStringLiteral() {
        ArrayList<String> patternValues = new ArrayList<>();
        patternValues.add("L");

        assertFalse(Pattern.matches(Pattern.TextOperator.NOT_STARTS_WITH, "L10", patternValues));
    }

    @Test
    public void shouldMatchNotStartsWithMultipleAlternatives() {
        ArrayList<String> patternValues = new ArrayList<>();
        patternValues.add("A");
        patternValues.add("C");

        assertTrue(Pattern.matches(Pattern.TextOperator.NOT_STARTS_WITH, "10", patternValues));
        assertFalse(Pattern.matches(Pattern.TextOperator.NOT_STARTS_WITH, "C10", patternValues));
        assertTrue(Pattern.matches(Pattern.TextOperator.NOT_STARTS_WITH, "10A", patternValues));
    }

    @Test
    public void shouldMatchEndsWithStringLiteral() {
        ArrayList<String> patternValues = new ArrayList<>();
        patternValues.add("L");

        assertTrue(Pattern.matches(Pattern.TextOperator.ENDS_WITH, "10L", patternValues));
    }

    @Test
    public void shouldNotMatchEndsWithStringLiteral() {
        ArrayList<String> patternValues = new ArrayList<>();
        patternValues.add("L");

        assertFalse(Pattern.matches(Pattern.TextOperator.ENDS_WITH, "L10", patternValues));
    }

    @Test
    public void shouldMatchEndsWithMultipleAlternatives() {
        ArrayList<String> patternValues = new ArrayList<>();
        patternValues.add("A");
        patternValues.add("C");

        assertTrue(Pattern.matches(Pattern.TextOperator.ENDS_WITH, "10A", patternValues));
        assertFalse(Pattern.matches(Pattern.TextOperator.ENDS_WITH, "10", patternValues));
        assertTrue(Pattern.matches(Pattern.TextOperator.ENDS_WITH, "10C", patternValues));
    }

    @Test
    public void shouldMatchNotEndsWithStringLiteral() {
        ArrayList<String> patternValues = new ArrayList<>();
        patternValues.add("L");

        assertTrue(Pattern.matches(Pattern.TextOperator.NOT_ENDS_WITH, "L10", patternValues));
    }

    @Test
    public void shouldNotMatchNotEndsWithStringLiteral() {
        ArrayList<String> patternValues = new ArrayList<>();
        patternValues.add("L");

        assertFalse(Pattern.matches(Pattern.TextOperator.NOT_ENDS_WITH, "10L", patternValues));
    }

    @Test
    public void shouldMatchNotEndsWithMultipleAlternatives() {
        ArrayList<String> patternValues = new ArrayList<>();
        patternValues.add("A");
        patternValues.add("C");

        assertTrue(Pattern.matches(Pattern.TextOperator.NOT_ENDS_WITH, "10", patternValues));
        assertFalse(Pattern.matches(Pattern.TextOperator.NOT_ENDS_WITH, "10A", patternValues));
        assertTrue(Pattern.matches(Pattern.TextOperator.NOT_ENDS_WITH, "501N", patternValues));
    }

    @Test
    public void shouldMatchInStringLiteralList() {
        ArrayList<String> patternValues = new ArrayList<>();
        patternValues.add("10");
        patternValues.add("50");
        patternValues.add("100");

        assertTrue(Pattern.matches(Pattern.TextOperator.IN, "10", patternValues));
    }

    @Test
    public void shouldNotMatchInStringLiteralList() {
        ArrayList<String> patternValues = new ArrayList<>();
        patternValues.add("10");
        patternValues.add("50");
        patternValues.add("100");

        assertFalse(Pattern.matches(Pattern.TextOperator.IN, "L10", patternValues));
    }

    @Test
    public void shouldMatchNotInStringLiteralList() {
        ArrayList<String> patternValues = new ArrayList<>();
        patternValues.add("10");
        patternValues.add("50");
        patternValues.add("100");

        assertTrue(Pattern.matches(Pattern.TextOperator.NOT_IN, "L10", patternValues));
    }

    @Test
    public void shouldNotMatchNotInStringLiteralList() {
        ArrayList<String> patternValues = new ArrayList<>();
        patternValues.add("10");
        patternValues.add("50");
        patternValues.add("100");

        assertFalse(Pattern.matches(Pattern.TextOperator.NOT_IN, "50", patternValues));
    }

    @Test
    public void shouldMatchGreaterThan() {
        Double patternValue = 50.0;
        assertTrue(Pattern.matches(Pattern.NumericalOperator.GREATER_THAN, 65.1234, patternValue));
    }

    @Test
    public void shouldNotMatchGreaterThan() {
        Double patternValue = 50.0;
        assertFalse(Pattern.matches(Pattern.NumericalOperator.GREATER_THAN, 50.0, patternValue));
    }

    @Test
    public void shouldMatchGreaterOrEqual() {
        Double patternValue = 50.0;
        assertTrue(Pattern.matches(Pattern.NumericalOperator.GREATER_OR_EQUAL, 50.0, patternValue));
    }

    @Test
    public void shouldMatchLessThan() {
        Double patternValue = 50.0;
        assertTrue(Pattern.matches(Pattern.NumericalOperator.LESS_THAN, 45.1234, patternValue));
    }

    @Test
    public void shouldNotMatchLessThan() {
        Double patternValue = 50.0;
        assertFalse(Pattern.matches(Pattern.NumericalOperator.LESS_THAN, 50.0, patternValue));
    }

    @Test
    public void shouldMatchLessOrEqual() {
        Double patternValue = 50.0;
        assertTrue(Pattern.matches(Pattern.NumericalOperator.LESS_OR_EQUAL, 50.0, patternValue));
    }

    @Test
    public void shouldMatchEqual() {
        Double patternValue = 50.0;
        assertTrue(Pattern.matches(Pattern.NumericalOperator.EQUAL, 50.0, patternValue));
    }

    @Test
    public void shouldNotMatchEqual() {
        Double patternValue = 50.0;
        assertFalse(Pattern.matches(Pattern.NumericalOperator.EQUAL, 55.0, patternValue));
    }

    @Test
    public void shouldMatchNotEqual() {
        Double patternValue = 50.0;
        assertTrue(Pattern.matches(Pattern.NumericalOperator.NOT_EQUAL, 50.1, patternValue));
    }

    @Test
    public void shouldNotMatchNotEqual() {
        Double patternValue = 50.0;
        assertFalse(Pattern.matches(Pattern.NumericalOperator.NOT_EQUAL, 50.0, patternValue));
    }

}
