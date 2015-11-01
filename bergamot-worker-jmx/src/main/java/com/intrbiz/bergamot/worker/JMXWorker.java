package com.intrbiz.bergamot.worker;

import com.intrbiz.bergamot.worker.config.JMXWorkerCfg;

/**
 * A worker to execute JMX checks
 */
public class JMXWorker extends DefaultWorker
{
    public JMXWorker()
    {
        super(JMXWorkerCfg.class, "/etc/bergamot/worker/jmx.xml", "bergamot-worker-jmx");
    }
    
    public static void main(String[] args) throws Exception
    {
        Worker worker = new JMXWorker();
        worker.start();
    }
}
