package org.opentripplanner.routing.core.vehicle_sharing;

import org.junit.Test;

import static com.google.common.collect.ImmutableSet.of;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ProviderFilterTest {

    private static final VehicleDescription VEHICLE = new CarDescription("id", 1.0, 2.0, null, null, new Provider(1, "Panek"));

    @Test
    public void testProvidersAllowedShouldPass() {
        // given
        ProviderFilter filter = ProviderFilter.providersAllowedFilter(of("Innogy", "Panek"));

        // then
        assertTrue(filter.isValid(VEHICLE));
    }

    @Test
    public void testProvidersDisallowedShouldFail() {
        // given
        ProviderFilter filter = ProviderFilter.providersDisallowedFilter(of("Innogy", "Panek"));

        // then
        assertFalse(filter.isValid(VEHICLE));
    }

    @Test
    public void testProvidersAllowedShouldFail() {
        // given
        ProviderFilter filter = ProviderFilter.providersAllowedFilter(of("Innogy"));

        // then
        assertFalse(filter.isValid(VEHICLE));
    }

    @Test
    public void testProvidersDisallowedShouldPass() {
        // given
        ProviderFilter filter = ProviderFilter.providersDisallowedFilter(of("Innogy"));

        // then
        assertTrue(filter.isValid(VEHICLE));
    }

    @Test
    public void testProvidersWithVehicleTypesAllowedShouldPass() {
        // given
        ProviderFilter filter = ProviderFilter.providersAllowedFilter(of("Panek_CAR"));

        // then
        assertTrue(filter.isValid(VEHICLE));
    }

    @Test
    public void testProvidersWithDifferentVehicleTypesAllowedShouldFail() {
        // given
        ProviderFilter filter = ProviderFilter.providersAllowedFilter(of("Panek_KICKSCOOTER"));

        // then
        assertFalse(filter.isValid(VEHICLE));
    }

    @Test
    public void testDifferentProvidersWithVehicleTypesAllowedShouldFail() {
        // given
        ProviderFilter filter = ProviderFilter.providersAllowedFilter(of("Innogy_CAR"));

        // then
        assertFalse(filter.isValid(VEHICLE));
    }

    @Test
    public void testMixedProvidersWithVehicleTypesAllowedShouldPass() {
        // given
        ProviderFilter filter = ProviderFilter.providersAllowedFilter(of("Innogy_CAR", "Panek", "HopCity_MOTORBIKE"));

        // then
        assertTrue(filter.isValid(VEHICLE));
    }

    @Test
    public void testProvidersWithVehicleTypesDisallowedShouldPass() {
        // given
        ProviderFilter filter = ProviderFilter.providersDisallowedFilter(of("Panek_KICKSCOOTER"));

        // then
        assertTrue(filter.isValid(VEHICLE));
    }
}