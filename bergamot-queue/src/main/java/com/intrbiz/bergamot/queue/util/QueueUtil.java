package com.intrbiz.bergamot.queue.util;

import java.util.List;

import com.intrbiz.bergamot.config.BrokerCfg;
import com.intrbiz.queue.QueueBrokerPool;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.hcq.HCQPool;
import com.intrbiz.queue.rabbit.RabbitPool;

public class QueueUtil
{
    public static void setupQueueBroker(List<BrokerCfg> brokerCfgs, String applicationName)
    {
        for (BrokerCfg brokerCfg : brokerCfgs)
        {
            setupQueueBroker(brokerCfg, applicationName);
        }
    }
    
    public static void setupQueueBroker(BrokerCfg brokerCfg, String applicationName)
    {
        registerPool(brokerCfg, createBroker(brokerCfg, applicationName));
    }
    
    private static void registerPool(BrokerCfg brokerCfg, QueueBrokerPool<?> pool)
    {
        if (brokerCfg.getRole() == null || "default".equals(brokerCfg.getRole()))
        {
            QueueManager.getInstance().registerDefaultBroker(pool);
        }
        else
        {
            QueueManager.getInstance().registerBroker(brokerCfg.getRole(), pool);
        }
    }
    
    private static QueueBrokerPool<?> createBroker(BrokerCfg brokerCfg, String applicationName)
    {
        if ("hcq".equals(brokerCfg.getType()))
        {
            return new HCQPool(brokerCfg.getAllUrls(), applicationName);
        }
        else
        {
            return new RabbitPool(brokerCfg.getUrl(), brokerCfg.getUsername(), brokerCfg.getPassword());
        }
    }
}
