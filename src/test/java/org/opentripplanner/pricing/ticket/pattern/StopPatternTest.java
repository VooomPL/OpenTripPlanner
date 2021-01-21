package org.opentripplanner.pricing.ticket.pattern;

import org.junit.Test;
import org.opentripplanner.model.FeedScopedId;
import org.opentripplanner.model.Stop;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StopPatternTest {

    @Test
    public void shouldNotMatchStopDueToNullTextAttribute() {
        StopPattern stopPattern = new StopPattern();
        stopPattern.addConstraint(StopPattern.StopAttribute.LONGITUDE, Pattern.NumericalOperator.GREATER_OR_EQUAL, 52.0034);

        Stop testedStop = new Stop();
        testedStop.setId(new FeedScopedId());

        assertFalse(stopPattern.matches(testedStop));
    }

    @Test
    public void shouldMatchStopWhenSingleTextConstraint() {
        StopPattern stopPattern = new StopPattern();
        stopPattern.addConstraint(StopPattern.StopAttribute.NAME, Pattern.TextOperator.IN, "Śmigłowca");

        Stop testedStop = new Stop();
        testedStop.setId(new FeedScopedId());
        testedStop.setName("Śmigłowca");

        assertTrue(stopPattern.matches(testedStop));
    }

    @Test
    public void shouldMatchStopWhenMultipleTextConstraint() {
        StopPattern stopPattern = new StopPattern();
        stopPattern.addConstraint(StopPattern.StopAttribute.NAME, Pattern.TextOperator.NOT_STARTS_WITH, "A");
        stopPattern.addConstraint(StopPattern.StopAttribute.NAME, Pattern.TextOperator.IN, "Śmigłowca");
        stopPattern.addConstraint(StopPattern.StopAttribute.NAME, Pattern.TextOperator.IN, "Metro Dworzec Gdański");

        Stop testedStop1 = new Stop();
        testedStop1.setId(new FeedScopedId());
        testedStop1.setName("Metro Dworzec Gdański");
        Stop testedStop2 = new Stop();
        testedStop2.setId(new FeedScopedId());
        testedStop2.setName("Śmigłowca");

        assertTrue(stopPattern.matches(testedStop1));
        assertTrue(stopPattern.matches(testedStop2));
    }

    @Test
    public void shouldNotMatchStopToConflictingConstraints() {
        StopPattern stopPattern = new StopPattern();
        stopPattern.addConstraint(StopPattern.StopAttribute.NAME, Pattern.TextOperator.NOT_STARTS_WITH, "A");
        stopPattern.addConstraint(StopPattern.StopAttribute.NAME, Pattern.TextOperator.IN, "Aluzyjna");

        Stop testedStop1 = new Stop();
        testedStop1.setId(new FeedScopedId());
        testedStop1.setName("Aluzyjna");

        assertFalse(stopPattern.matches(testedStop1));
    }

}
