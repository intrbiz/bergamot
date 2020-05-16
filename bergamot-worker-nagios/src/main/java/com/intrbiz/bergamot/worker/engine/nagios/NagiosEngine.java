package com.intrbiz.bergamot.worker.engine.nagios;

import com.intrbiz.bergamot.BergamotVersion;
import com.intrbiz.bergamot.worker.engine.AbstractCheckEngine;

/**
 * Nagios check engine
 */
public class NagiosEngine extends AbstractCheckEngine
{
    public static final String NAME = "nagios";

    public NagiosEngine()
    {
        super(BergamotVersion.NAME, NAME, true, new NagiosExecutor());
    }
}
