package org.opentripplanner.pricing.transit.ticket.pattern;

import org.junit.Test;
import org.opentripplanner.model.FeedScopedId;
import org.opentripplanner.model.Route;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RoutePatternTest {

    @Test
    public void shouldNotMatchRouteDueToNullTextAttribute() {
        RoutePattern routePattern = new RoutePattern();
        routePattern.addConstraint(RoutePattern.RouteAttribute.SHORT_NAME, Pattern.TextOperator.NOT_STARTS_WITH, "A");
        routePattern.addConstraint(RoutePattern.RouteAttribute.TYPE, Pattern.NumericalOperator.GREATER_THAN, 1.0);
        routePattern.addConstraint(RoutePattern.RouteAttribute.TYPE, Pattern.NumericalOperator.LESS_OR_EQUAL, 5.5);

        Route testedRoute = new Route();
        testedRoute.setId(new FeedScopedId());
        testedRoute.setType(2);

        assertFalse(routePattern.matches(testedRoute));
    }

    @Test
    public void shouldMatchRouteWhenSingleTextConstraint() {
        RoutePattern routePattern = new RoutePattern();
        routePattern.addConstraint(RoutePattern.RouteAttribute.SHORT_NAME, Pattern.TextOperator.NOT_STARTS_WITH, "A");
        routePattern.addConstraint(RoutePattern.RouteAttribute.TYPE, Pattern.NumericalOperator.GREATER_THAN, 1.0);
        routePattern.addConstraint(RoutePattern.RouteAttribute.TYPE, Pattern.NumericalOperator.LESS_OR_EQUAL, 5.5);

        Route testedRoute = new Route();
        testedRoute.setId(new FeedScopedId());
        testedRoute.setShortName("510");
        testedRoute.setType(2);

        assertTrue(routePattern.matches(testedRoute));
    }

    @Test
    public void shouldMatchRouteWhenMultipleTextConstraint() {
        RoutePattern routePattern = new RoutePattern();
        routePattern.addConstraint(RoutePattern.RouteAttribute.SHORT_NAME, Pattern.TextOperator.NOT_STARTS_WITH, "A");
        routePattern.addConstraint(RoutePattern.RouteAttribute.SHORT_NAME, Pattern.TextOperator.IN, "510");
        routePattern.addConstraint(RoutePattern.RouteAttribute.SHORT_NAME, Pattern.TextOperator.IN, "B10");

        Route testedRoute1 = new Route();
        testedRoute1.setId(new FeedScopedId());
        testedRoute1.setShortName("510");
        Route testedRoute2 = new Route();
        testedRoute2.setId(new FeedScopedId());
        testedRoute2.setShortName("B10");

        assertTrue(routePattern.matches(testedRoute1));
        assertTrue(routePattern.matches(testedRoute2));
    }

    @Test
    public void shouldNotMatchRouteDueToNotStartsWithConstraint() {
        RoutePattern routePattern = new RoutePattern();
        routePattern.addConstraint(RoutePattern.RouteAttribute.SHORT_NAME, Pattern.TextOperator.NOT_STARTS_WITH, "A");
        routePattern.addConstraint(RoutePattern.RouteAttribute.SHORT_NAME, Pattern.TextOperator.IN, "510");
        routePattern.addConstraint(RoutePattern.RouteAttribute.SHORT_NAME, Pattern.TextOperator.IN, "A10");

        Route testedRoute = new Route();
        testedRoute.setId(new FeedScopedId());
        testedRoute.setShortName("A10");

        assertFalse(routePattern.matches(testedRoute));
    }

}
