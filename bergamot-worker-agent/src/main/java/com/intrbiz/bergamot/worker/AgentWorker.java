package com.intrbiz.bergamot.worker;

import com.intrbiz.bergamot.worker.config.AgentWorkerCfg;
 
/**
 * A worker to execute SNMP check engines
 */
public class AgentWorker extends DefaultWorker
{
    public AgentWorker()
    {
        super(AgentWorkerCfg.class, "/etc/bergamot/worker/agent.xml", "bergamot-worker-agent");
    }
    
    public static void main(String[] args) throws Exception
    {
        Worker worker = new AgentWorker();
        worker.start();
    }
}
