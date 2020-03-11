package com.intrbiz.bergamot.worker.engine.nagios;

import com.intrbiz.bergamot.worker.engine.AbstractEngine;

/**
 * Nagios check engine
 */
public class NagiosEngine extends AbstractEngine
{
    public static final String NAME = "nagios";

    public NagiosEngine()
    {
        super(NAME, new NagiosExecutor());
    }
}
