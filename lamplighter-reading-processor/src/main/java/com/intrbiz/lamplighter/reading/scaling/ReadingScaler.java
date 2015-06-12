package com.intrbiz.lamplighter.reading.scaling;

import com.intrbiz.gerald.polyakov.Reading;

/**
 * Handle the scaling of a particular unit, to a common base unit 
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
    Reading scaleReading(Reading reading);
}
