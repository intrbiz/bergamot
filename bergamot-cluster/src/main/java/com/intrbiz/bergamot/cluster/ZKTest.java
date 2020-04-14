package com.intrbiz.bergamot.cluster;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.intrbiz.bergamot.cluster.registry.AgentRegistry;
import com.intrbiz.bergamot.cluster.zookeeper.ZooKeeperManager;

public class ZKTest
{

    public static void main(String[] args) throws Exception
    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        //
        ZooKeeperManager zkm = new ZooKeeperManager("127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183");
        //
        AgentRegistry reg = new AgentRegistry(zkm.getZooKeeper());
        //
        Thread.sleep(300_000);
    }
    
}
