package com.intrbiz.bergamot.accounting;

import org.apache.log4j.Logger;

import com.intrbiz.accounting.consumer.AsyncConsumer;
import com.intrbiz.accounting.model.AccountingEvent;
import com.intrbiz.bergamot.accounting.model.BergamotAccountingEvent;
import com.intrbiz.bergamot.queue.AccountingQueue;
import com.intrbiz.queue.Producer;

/**
 * Enqueue accounting events into a message queue
 */
public class BergamotAccountingQueueConsumer extends AsyncConsumer
{
    private AccountingQueue queue;
    
    private Producer<BergamotAccountingEvent> producer;
    
    private Logger logger = Logger.getLogger(BergamotAccountingQueueConsumer.class);
    
    public BergamotAccountingQueueConsumer()
    {
        super();
        this.queue = AccountingQueue.open();
        this.producer = this.queue.publishAccountingEvents();
    }

    @Override
    protected void processAccountingEvent(Class<?> source, AccountingEvent event)
    {
        if (event instanceof BergamotAccountingEvent)
        {
            try
            {
                this.producer.publish((BergamotAccountingEvent) event);
            }
            catch (Exception e)
            {
                logger.error("Failed to enqueue accounting event: " + event.getClass() + " " + event.toString());
            }
        }
    }
}
