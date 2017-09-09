package com.intrbiz.bergamot.queue;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.intrbiz.bergamot.accounting.model.BergamotAccountingEvent;
import com.intrbiz.bergamot.accounting.model.LoginAccountingEvent;
import com.intrbiz.queue.Producer;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.hcq.HCQPool;

public class HCQTest
{
    public static void main(String[] args) throws Exception
    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.TRACE);
        //
        QueueManager.getInstance().registerDefaultBroker(new HCQPool(new String[] { 
                "ws://127.0.0.1:1543/hcq",
                "ws://127.0.0.1:1544/hcq",
                "ws://127.0.0.1:1545/hcq"
        }, "test"));
        //
        AccountingQueue q = AccountingQueue.open();
        System.out.println("Q:" + q);
        //
        q.consumeAccountingEvents((headers, event) -> System.out.println("Event: " + event));
        //
        Producer<BergamotAccountingEvent> p = q.publishAccountingEvents();
        while (true)
        {
            try
            {
                p.publish(new LoginAccountingEvent());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            Thread.sleep(1000);
        }
    }
}
