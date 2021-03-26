package org.opentripplanner.pricing.transit.ticket.pattern;

import org.junit.Before;
import org.junit.Test;
import org.opentripplanner.model.FeedScopedId;
import org.opentripplanner.model.Route;
import org.opentripplanner.model.Stop;
import org.opentripplanner.pricing.transit.trip.model.FareSwitch;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FareSwitchPatternTest {

    private RoutePattern previousRoutePattern, futureRoutePattern;
    private StopPattern previousStopPattern, futureStopPattern;


    @Before
    public void init() {
        previousRoutePattern = new RoutePattern();
        futureRoutePattern = new RoutePattern();
        previousStopPattern = new StopPattern();
        futureStopPattern = new StopPattern();

        /*
            Inspired by AT Routes in Bydgoszcz:
            “previous_fare”: “Route.route_short_name equals 81“
            “matching fares”: {“Route.route_short_name equals 3“, “Route.route_short_name equals 5“}
        */

        previousRoutePattern.addConstraint(RoutePattern.RouteAttribute.SHORT_NAME, Pattern.TextOperator.IN, "81");
        futureRoutePattern.addConstraint(RoutePattern.RouteAttribute.SHORT_NAME, Pattern.TextOperator.IN, "3");
        futureRoutePattern.addConstraint(RoutePattern.RouteAttribute.SHORT_NAME, Pattern.TextOperator.IN, "5");
    }

    @Test
    public void shouldMatchFareSwitchingConstraints() {
        FareSwitchPattern pattern = new FareSwitchPattern(previousRoutePattern, futureRoutePattern, previousStopPattern, futureStopPattern, false);

        Route previousRoute = new Route();
        previousRoute.setId(new FeedScopedId());
        previousRoute.setShortName("81");
        Stop previousStop = new Stop();
        previousStop.setId(new FeedScopedId());
        Route futureRoute = new Route();
        futureRoute.setId(new FeedScopedId());
        futureRoute.setShortName("3");
        Stop futureStop = new Stop();
        futureStop.setId(new FeedScopedId());

        FareSwitch validatedSwitch = new FareSwitch(previousRoute, futureRoute, previousStop, futureStop);

        assertTrue(pattern.matches(validatedSwitch));
    }

    @Test
    public void shouldMatchReversedConstraints() {
        FareSwitchPattern pattern = new FareSwitchPattern(previousRoutePattern, futureRoutePattern, previousStopPattern, futureStopPattern, true);

        Route previousRoute = new Route();
        previousRoute.setId(new FeedScopedId());
        previousRoute.setShortName("3");
        Stop previousStop = new Stop();
        previousStop.setId(new FeedScopedId());
        Route futureRoute = new Route();
        futureRoute.setId(new FeedScopedId());
        futureRoute.setShortName("81");
        Stop futureStop = new Stop();
        futureStop.setId(new FeedScopedId());

        FareSwitch validatedSwitch = new FareSwitch(previousRoute, futureRoute, previousStop, futureStop);

        assertTrue(pattern.matches(validatedSwitch));
    }

    @Test
    public void shouldNotMatchFareSwitchingConstraints() {
        FareSwitchPattern pattern = new FareSwitchPattern(previousRoutePattern, futureRoutePattern, previousStopPattern, futureStopPattern, false);

        Route previousRoute = new Route();
        previousRoute.setId(new FeedScopedId());
        previousRoute.setShortName("81");
        Stop previousStop = new Stop();
        previousStop.setId(new FeedScopedId());
        Route futureRoute = new Route();
        futureRoute.setId(new FeedScopedId());
        futureRoute.setShortName("512");
        Stop futureStop = new Stop();
        futureStop.setId(new FeedScopedId());

        FareSwitch validatedSwitch = new FareSwitch(previousRoute, futureRoute, previousStop, futureStop);

        assertFalse(pattern.matches(validatedSwitch));
    }

    @Test
    public void shouldNotMatchReversedConstraints() {
        FareSwitchPattern pattern = new FareSwitchPattern(previousRoutePattern, futureRoutePattern, previousStopPattern, futureStopPattern, false);

        Route previousRoute = new Route();
        previousRoute.setId(new FeedScopedId());
        previousRoute.setShortName("3");
        Stop previousStop = new Stop();
        Route futureRoute = new Route();
        futureRoute.setId(new FeedScopedId());
        futureRoute.setShortName("81");
        Stop futureStop = new Stop();

        FareSwitch validatedSwitch = new FareSwitch(previousRoute, futureRoute, previousStop, futureStop);

        assertFalse(pattern.matches(validatedSwitch));
    }

    @Test
    public void shouldMatchWithNullConstraints() {
        FareSwitchPattern pattern = new FareSwitchPattern(null, null, null, null, false);

        Route previousRoute = new Route();
        previousRoute.setId(new FeedScopedId());
        previousRoute.setShortName("81");
        Stop previousStop = new Stop();
        Route futureRoute = new Route();
        futureRoute.setId(new FeedScopedId());
        futureRoute.setShortName("512");
        Stop futureStop = new Stop();

        FareSwitch validatedSwitch = new FareSwitch(previousRoute, futureRoute, previousStop, futureStop);

        assertTrue(pattern.matches(validatedSwitch));
    }

    @Test
    public void shouldMatchFareSwitchingWithStopConstraints() {
        futureStopPattern.addConstraint(StopPattern.StopAttribute.NAME, Pattern.TextOperator.IN, "Śmigłowca");

        FareSwitchPattern pattern = new FareSwitchPattern(previousRoutePattern, futureRoutePattern, previousStopPattern, futureStopPattern, false);

        Route previousRoute = new Route();
        previousRoute.setId(new FeedScopedId());
        previousRoute.setShortName("81");
        Stop previousStop = new Stop();
        previousStop.setId(new FeedScopedId());
        Route futureRoute = new Route();
        futureRoute.setId(new FeedScopedId());
        futureRoute.setShortName("3");
        Stop futureStop = new Stop();
        futureStop.setId(new FeedScopedId());
        futureStop.setName("Śmigłowca");

        FareSwitch validatedSwitch = new FareSwitch(previousRoute, futureRoute, previousStop, futureStop);

        assertTrue(pattern.matches(validatedSwitch));
    }

    @Test
    public void shouldNotMatchFareSwitchingWithStopConstraints() {
        futureStopPattern.addConstraint(StopPattern.StopAttribute.NAME, Pattern.TextOperator.IN, "Śmigłowca");

        FareSwitchPattern pattern = new FareSwitchPattern(previousRoutePattern, futureRoutePattern, previousStopPattern, futureStopPattern, false);

        Route previousRoute = new Route();
        previousRoute.setId(new FeedScopedId());
        previousRoute.setShortName("81");
        Stop previousStop = new Stop();
        previousStop.setId(new FeedScopedId());
        Route futureRoute = new Route();
        futureRoute.setId(new FeedScopedId());
        futureRoute.setShortName("3");
        Stop futureStop = new Stop();
        futureStop.setId(new FeedScopedId());
        futureStop.setName("Dworzec Gdański");

        FareSwitch validatedSwitch = new FareSwitch(previousRoute, futureRoute, previousStop, futureStop);

        assertFalse(pattern.matches(validatedSwitch));
    }

}
