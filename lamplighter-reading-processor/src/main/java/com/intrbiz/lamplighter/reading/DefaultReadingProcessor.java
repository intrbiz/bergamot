package com.intrbiz.lamplighter.reading;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.message.reading.ReadingParcelMO;
import com.intrbiz.bergamot.result.matcher.Matchers;

public class DefaultReadingProcessor extends AbstractReadingProcessor
{
    private Logger logger = Logger.getLogger(DefaultReadingProcessor.class);
    
    private Matchers matchers = new Matchers();

    public DefaultReadingProcessor()
    {
        super();
    }

    @Override
    public void processReadings(ReadingParcelMO readings)
    {
        if (logger.isTraceEnabled()) logger.trace("Processing readings: " + readings);
    }
}
