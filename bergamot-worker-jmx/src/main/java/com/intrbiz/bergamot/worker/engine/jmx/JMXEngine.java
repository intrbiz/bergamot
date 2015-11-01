package com.intrbiz.bergamot.worker.engine.jmx;

import com.intrbiz.bergamot.check.jmx.JMXChecker;
import com.intrbiz.bergamot.worker.engine.AbstractEngine;

/**
 * A dedicated JMX check engine
 */
public class JMXEngine extends AbstractEngine
{
    public static final String NAME = "jmx";
    
    private JMXChecker checker;

    public JMXEngine()
    {
        super(NAME);
    }

    @Override
    protected void configure() throws Exception
    {
        super.configure();
        // add the default executor
        if (this.executors.isEmpty())
        {
            this.addExecutor(new ScriptedJMXExecutor());
        }
        // setup the checker
        this.checker = new JMXChecker();
    }
    
    public JMXChecker getChecker()
    {
        return this.checker;
    }
}
