package com.intrbiz.bergamot.watcher.engine.snmp;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.watcher.engine.AbstractEngine;
import com.intrbiz.snmp.SNMPTransport;

/**
 * Watch for SNMP traps using SNMP-IB
 */
public class SNMPEngine extends AbstractEngine
{
    public static final String NAME = "snmp";
    
    private SNMPTransport transport;
    
    private Thread transportThread;

    public SNMPEngine()
    {
        super(NAME);
    }

    @Override
    protected void configure() throws Exception
    {
        super.configure();
        if (this.executor.isEmpty())
        {
            this.addListener(new LinkStateExecutor());
        }
    }
    
    @Override
    public void start() throws Exception
    {
        // setup the transport
        int port = this.getWatcher().getConfiguration().getIntParameterValue("snmp-port", 8162);
        Logger.getLogger(SNMPEngine.class).info("Listening for SNMP traps on port " + port);
        this.transport = SNMPTransport.open(port);
        this.transportThread = new Thread(this.transport, "SNMP-Transport");
        this.transportThread.start();
        // setup queues etc
        super.start();
    }

    public SNMPTransport getTransport()
    {
        return this.transport;
    }
}
