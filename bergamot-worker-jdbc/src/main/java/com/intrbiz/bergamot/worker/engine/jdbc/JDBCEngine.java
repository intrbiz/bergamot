package com.intrbiz.bergamot.worker.engine.jdbc;

import com.intrbiz.bergamot.BergamotVersion;
import com.intrbiz.bergamot.check.jdbc.JDBCChecker;
import com.intrbiz.bergamot.worker.engine.AbstractCheckEngine;

/**
 * A dedicated JDBC check engine
 */
public class JDBCEngine extends AbstractCheckEngine
{
    public static final String NAME = "jdbc";
    
    private JDBCChecker checker;

    public JDBCEngine()
    {
        super(BergamotVersion.NAME, NAME, true, new ScriptedJDBCExecutor());
        // setup the checker
        this.checker = new JDBCChecker();
    }
    
    public JDBCChecker getChecker()
    {
        return this.checker;
    }
}
