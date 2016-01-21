package com.intrbiz.bergamot.worker;

import com.intrbiz.bergamot.worker.DefaultWorker;
import com.intrbiz.bergamot.worker.Worker;
import com.intrbiz.bergamot.worker.config.SSHWorkerCfg;

/**
 * A worker to execute nagios check engines
 */
public class SSHWorker extends DefaultWorker
{
    public SSHWorker()
    {
        super(SSHWorkerCfg.class, "/etc/bergamot/worker/nagios.xml", "bergamot-worker-nagios");
    }
    
    public static void main(String[] args) throws Exception
    {
        Worker worker = new SSHWorker();
        worker.start();
    }
}
