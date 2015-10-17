package com.intrbiz.bergamot.worker;

import com.intrbiz.bergamot.worker.DefaultWorker;
import com.intrbiz.bergamot.worker.Worker;
import com.intrbiz.bergamot.worker.config.NagiosWorkerCfg;

/**
 * A worker to execute nagios check engines
 */
public class NagiosWorker extends DefaultWorker
{
    public NagiosWorker()
    {
        super(NagiosWorkerCfg.class, "/etc/bergamot/worker/nagios.xml", "bergamot-worker-nagios");
    }
    
    public static void main(String[] args) throws Exception
    {
        Worker worker = new NagiosWorker();
        worker.start();
    }
}
