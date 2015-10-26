package com.intrbiz.bergamot.accounting.consumer;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.intrbiz.accounting.consumer.AsyncConsumer;
import com.intrbiz.accounting.model.AccountingEvent;
import com.intrbiz.bergamot.accounting.io.BergamotAccountingTranscoder;
import com.intrbiz.bergamot.accounting.model.BergamotAccountingEvent;

public class BergamotLoggingConsumer extends AsyncConsumer
{
    private Logger logger = Logger.getLogger(BergamotLoggingConsumer.class);
    
    private BergamotAccountingTranscoder transcoder = BergamotAccountingTranscoder.getDefault();
    
    private Level level = Level.DEBUG;
    
    public BergamotLoggingConsumer()
    {
        super();
    }
    
    public BergamotLoggingConsumer(Level level)
    {
        super();
        this.level = level;
    }

    @Override
    protected void processAccountingEvent(Class<?> source, AccountingEvent event)
    {
        if (logger.isDebugEnabled())
        {
            if (event instanceof BergamotAccountingEvent)
            {
                logger.log(this.level, "Accounting from " + source + " " + this.transcoder.encodeToString((BergamotAccountingEvent) event));
            }
        }
    }
}
