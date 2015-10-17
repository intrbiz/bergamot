package com.intrbiz.bergamot.worker;

import com.intrbiz.bergamot.worker.config.DummyWorkerCfg;
 
/**
 * A worker to execute dummy checks, for testing purposes
 */
public class DummyWorker extends DefaultWorker
{
    public DummyWorker()
    {
        super(DummyWorkerCfg.class, "/etc/bergamot/worker/dummy.xml", "bergamot-worker-dummy");
    }
    
    public static void main(String[] args) throws Exception
    {
        Worker worker = new DummyWorker();
        worker.start();
    }
}
