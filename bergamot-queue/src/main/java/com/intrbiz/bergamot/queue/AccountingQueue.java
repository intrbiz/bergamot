package com.intrbiz.bergamot.queue;



import com.intrbiz.bergamot.accounting.model.BergamotAccountingEvent;
import com.intrbiz.bergamot.queue.impl.RabbitAccountingQueue;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.Producer;
import com.intrbiz.queue.QueueAdapter;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.name.NullKey;

/**
 * Send accounting events
 */
public abstract class AccountingQueue extends QueueAdapter
{    
    static
    {
        RabbitAccountingQueue.register();
    }
    
    public static AccountingQueue open()
    {
        return QueueManager.getInstance().queueAdapter(AccountingQueue.class);
    }
    
    public abstract Producer<BergamotAccountingEvent> publishAccountingEvents();
    
    public abstract Consumer<BergamotAccountingEvent, NullKey> consumeAccountingEvents(DeliveryHandler<BergamotAccountingEvent> handler);
}
