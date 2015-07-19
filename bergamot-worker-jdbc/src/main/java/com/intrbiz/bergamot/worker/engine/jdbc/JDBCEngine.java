package com.intrbiz.bergamot.worker.engine.jdbc;

import com.intrbiz.bergamot.check.jdbc.JDBCChecker;
import com.intrbiz.bergamot.worker.engine.AbstractEngine;

/**
 * A dedicated JDBC check engine
 */
public class JDBCEngine extends AbstractEngine
{
    public static final String NAME = "jdbc";
    
    private JDBCChecker checker;

    public JDBCEngine()
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
            this.addExecutor(new ScriptedJDBCExecutor());
        }
        // setup the checker
        this.checker = new JDBCChecker();
    }
    
    public JDBCChecker getChecker()
    {
        return this.checker;
    }
}
