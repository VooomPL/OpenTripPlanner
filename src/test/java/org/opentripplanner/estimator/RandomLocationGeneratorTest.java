package org.opentripplanner.estimator;

import org.junit.Test;
import org.opentripplanner.common.model.GenericLocation;
import org.opentripplanner.estimator.utils.RandomLocationGenerator;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RandomLocationGeneratorTest {

    @Test
    public void shouldReturnModifiedLocationWhenMaxValueExceeded() {
        Random randomMock = mock(Random.class);
        when(randomMock.nextDouble()).thenReturn(0.2);
        RandomLocationGenerator locationGenerator = new RandomLocationGenerator(randomMock);

        GenericLocation location = locationGenerator.generateRandomLocation(new GenericLocation(27, 180), 0.01);
        assertEquals(-179.99, location.lng, 0.009);
    }

    @Test
    public void shouldReturnModifiedLocationWhenMinValueExceeded() {
        Random randomMock = mock(Random.class);
        when(randomMock.nextDouble()).thenReturn(0.7);
        RandomLocationGenerator locationGenerator = new RandomLocationGenerator(randomMock);

        GenericLocation location = locationGenerator.generateRandomLocation(new GenericLocation(27, -180), 0.01);
        assertEquals(179.99, location.lng, 0.009);
    }

    @Test
    public void shouldReturnTheSameLocation() {
        Random randomMock = mock(Random.class);
        when(randomMock.nextDouble()).thenReturn(0.0);
        RandomLocationGenerator locationGenerator = new RandomLocationGenerator(randomMock);

        GenericLocation location = locationGenerator.generateRandomLocation(new GenericLocation(27, 52.5580098), 0.01);
        assertEquals(52.5580098, location.lng, 0);
    }

}
