package org.opentripplanner.updater.vehicle_sharing.vehicles_positions;

import org.junit.Test;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.internal.util.reflection.Whitebox;
import org.opentripplanner.routing.core.vehicle_sharing.Provider;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class VehiclePositionsGetterTest {

    @Test
    public void shouldReturnNonEmptyListOfResponsiveProviders(){
        VehiclePositionsMapper mapper = mock(VehiclePositionsMapper.class);

        //given
        Map<Provider, Long> numberOfVehiclesPerProvider = Collections.singletonMap(new Provider(1, "Innogy"), 125L);
        when(mapper.getNumberOfMappedVehiclesPerProvider()).thenReturn(numberOfVehiclesPerProvider);

        VehiclePositionsGetter vehiclePositionsGetter = new VehiclePositionsGetter();
        Whitebox.setInternalState(vehiclePositionsGetter, "mapper", mapper);

        //when
        List<Provider> responsiveProviders = vehiclePositionsGetter.getResponsiveProviders();

        //then
        assertEquals(1, responsiveProviders.size());
        assertEquals(1, responsiveProviders.get(0).getProviderId());
        assertEquals("Innogy", responsiveProviders.get(0).getProviderName());
    }

    @Test
    public void shouldReturnEmptyListOfResponsiveProvidersDueToNoVehiclesFromProvider(){
        VehiclePositionsMapper mapper = mock(VehiclePositionsMapper.class);

        //given
        Map<Provider, Long> numberOfVehiclesPerProvider = Collections.singletonMap(new Provider(1, "Panek"), 0L);
        when(mapper.getNumberOfMappedVehiclesPerProvider()).thenReturn(numberOfVehiclesPerProvider);

        VehiclePositionsGetter vehiclePositionsGetter = new VehiclePositionsGetter();
        Whitebox.setInternalState(vehiclePositionsGetter, "mapper", mapper);

        //when
        List<Provider> responsiveProviders = vehiclePositionsGetter.getResponsiveProviders();

        //then
        assertTrue(responsiveProviders.isEmpty());
    }

    @Test
    public void shouldReturnEmptyListOfResponsiveProvidersDueToNoProviders(){
        VehiclePositionsMapper mapper = mock(VehiclePositionsMapper.class);

        //given
        when(mapper.getNumberOfMappedVehiclesPerProvider()).thenReturn(Collections.emptyMap());

        VehiclePositionsGetter vehiclePositionsGetter = new VehiclePositionsGetter();
        Whitebox.setInternalState(vehiclePositionsGetter, "mapper", mapper);

        //when
        List<Provider> responsiveProviders = vehiclePositionsGetter.getResponsiveProviders();

        //then
        assertTrue(responsiveProviders.isEmpty());
    }
}
