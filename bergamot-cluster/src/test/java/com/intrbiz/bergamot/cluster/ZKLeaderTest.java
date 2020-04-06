package com.intrbiz.bergamot.cluster;

import java.util.UUID;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.intrbiz.bergamot.cluster.election.LeaderElector;
import com.intrbiz.bergamot.cluster.zookeeper.ZooKeeperManager;
import com.intrbiz.bergamot.cluster.zookeeper.ZooKeeperManager.ZooKeeperState;

public class ZKLeaderTest
{
    public static void main(String[] args) throws Exception
    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        //
        ZooKeeperManager zooKeeper = new ZooKeeperManager("127.0.0.1:2181");
        zooKeeper.listen((state) -> {
            if (state == ZooKeeperState.EXPIRED)
            {
                System.exit(1);
            }
        });
        //
        LeaderElector elector = new LeaderElector(zooKeeper.getZooKeeper(), UUID.randomUUID());
        System.out.println("Elected: " + elector.elect((state) -> System.out.println("Promoted: " + state)));
        //
        Thread.sleep(300_000);
    }
}
