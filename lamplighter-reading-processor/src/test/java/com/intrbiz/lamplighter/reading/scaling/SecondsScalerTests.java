package com.intrbiz.lamplighter.reading.scaling;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.Test;

public class SecondsScalerTests
{

    @Test
    public void msToMsFactor()
    {
        double factor = SecondsScaler.toNanos("ms") / SecondsScaler.toNanos("ms");
        assertThat(factor, is(equalTo(1D)));
    }
    
    @Test
    public void msToNsFactor()
    {
        double factor = SecondsScaler.toNanos("ms") / SecondsScaler.toNanos("ns");
        assertThat(factor, is(equalTo(1000_000D)));
    }
    
    @Test
    public void msToSFactor()
    {
        double factor = SecondsScaler.toNanos("ms") / SecondsScaler.toNanos("s");
        assertThat(factor, is(equalTo(1D/1000D)));
    }
    
    @Test
    public void sToMsFactor()
    {
        double factor = SecondsScaler.toNanos("s") / SecondsScaler.toNanos("ms");
        assertThat(factor, is(equalTo(1000D)));
    }

}
