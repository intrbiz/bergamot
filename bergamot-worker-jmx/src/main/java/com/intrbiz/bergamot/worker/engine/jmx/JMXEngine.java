package com.intrbiz.bergamot.worker.engine.jmx;

import com.intrbiz.bergamot.BergamotVersion;
import com.intrbiz.bergamot.check.jmx.JMXChecker;
import com.intrbiz.bergamot.worker.engine.AbstractCheckEngine;

/**
 * A dedicated JMX check engine
 */
public class JMXEngine extends AbstractCheckEngine
{
    public static final String NAME = "jmx";
    
    private JMXChecker checker;

    public JMXEngine()
    {
        super(BergamotVersion.NAME, NAME, true, new ScriptedJMXExecutor());
        // setup the checker
        this.checker = new JMXChecker();
    }
    
    public JMXChecker getChecker()
    {
        return this.checker;
    }
}
