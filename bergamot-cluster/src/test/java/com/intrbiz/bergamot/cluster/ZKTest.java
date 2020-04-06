package com.intrbiz.bergamot.cluster;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.intrbiz.bergamot.cluster.registry.WorkerRegistar;
import com.intrbiz.bergamot.cluster.registry.WorkerRegistry;
import com.intrbiz.bergamot.cluster.zookeeper.ZooKeeperManager;
import com.intrbiz.bergamot.cluster.zookeeper.ZooKeeperManager.ZooKeeperState;
import com.intrbiz.bergamot.model.message.cluster.WorkerRegistration;

public class ZKTest
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
        WorkerRegistry workerRegistry = new WorkerRegistry(zooKeeper.getZooKeeper());
        workerRegistry.listen((event) -> System.out.println("Got worker event " + event));
        //
        UUID id = UUID.randomUUID();
        WorkerRegistration worker = new WorkerRegistration(id, System.currentTimeMillis(), false, "Test", "", "", null, null, new HashSet<>(Arrays.asList("dummy")));
        //
        WorkerRegistar client = new WorkerRegistar(zooKeeper.getZooKeeper());
        client.registerWorker(worker);
        //
        Thread.sleep(1000);
        System.out.println(workerRegistry.getRouteTable());
        //
        worker.availableEngines("test");
        client.reregisterWorker(worker);
        Thread.sleep(1000);
        System.out.println(workerRegistry.getRouteTable());
        //
        Thread.sleep(300_000);
    }
}
