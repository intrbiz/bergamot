package com.intrbiz.bergamot.worker.engine.snmp;

import com.intrbiz.bergamot.worker.engine.AbstractEngine;
import com.intrbiz.snmp.SNMPTransport;

/**
 * Execute SNMP checks using SNMP-IB
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
        if (this.executors.isEmpty())
        {
            this.addExecutor(new SNMPExecutor());
        }
    }
    
    @Override
    public void start() throws Exception
    {
        // setup the transport
        this.transport = SNMPTransport.open();
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
