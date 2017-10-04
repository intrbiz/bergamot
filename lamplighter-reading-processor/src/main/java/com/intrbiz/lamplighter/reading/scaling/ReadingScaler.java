package com.intrbiz.lamplighter.reading.scaling;

import com.intrbiz.gerald.polyakov.Reading;

/**
 * Handle the scaling of a particular unit, so that readings are always logged in a common unit 
 */
public interface ReadingScaler
{
    /**
     * What units can we scale
     */
    String[] getUnits();
    
    /**
     * Scale the given reading
     */
    <T extends Reading> T scaleReading(T reading, String toUnit);
}
