package com.intrbiz.bergamot.worker.engine.snmp;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.worker.engine.AbstractEngine;
import com.intrbiz.bergamot.worker.engine.EngineContext;
import com.intrbiz.snmp.SNMPTransport;

/**
 * Execute SNMP checks using SNMP-IB
 */
public class SNMPEngine extends AbstractEngine
{
    private static final Logger logger = Logger.getLogger(SNMPEngine.class);
    
    public static final String NAME = "snmp";
    
    private SNMPTransport transport;
    
    private Thread transportThread;

    public SNMPEngine()
    {
        super(NAME,
                new ScriptedSNMPExecutor(),
                new GetSNMPExecutor());
    }
    
    @Override
    public void doPrepare(EngineContext engineContext) throws Exception
    {
        // setup the transport
        int port = engineContext.getIntParameter("snmp-port", 8161);
        logger.info("Querying SNMP agents from port " + port);
        this.transport = SNMPTransport.open(port);
        this.transportThread = new Thread(this.transport, "SNMP-Transport");
    }
    
    @Override
    public void doStart(EngineContext engineContext) throws Exception
    {
        // start the transport
        this.transportThread.start();
    }

    public SNMPTransport getTransport()
    {
        return this.transport;
    }
}
