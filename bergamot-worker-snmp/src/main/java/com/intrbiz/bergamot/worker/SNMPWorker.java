package com.intrbiz.bergamot.worker;

import com.intrbiz.bergamot.worker.config.SNMPWorkerCfg;
 
/**
 * A worker to execute SNMP check engines
 */
public class SNMPWorker extends DefaultWorker
{
    public SNMPWorker()
    {
        super(SNMPWorkerCfg.class, "/etc/bergamot/worker/snmp.xml", "bergamot-worker-snmp");
    }
    
    public static void main(String[] args) throws Exception
    {
        Worker worker = new SNMPWorker();
        worker.start();
    }
}
