package com.intrbiz.bergamot.worker.engine.http;

import com.intrbiz.bergamot.worker.check.http.HTTPChecker;
import com.intrbiz.bergamot.worker.engine.AbstractEngine;

/**
 * A dedicated HTTP check engine
 */
public class HTTPEngine extends AbstractEngine
{
    public static final String NAME = "http";
    
    private HTTPChecker checker;

    public HTTPEngine()
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
            this.addExecutor(new HTTPExecutor());
        }
        // setup our checker
        this.checker = new HTTPChecker(this.getWorker().getConfiguration().getThreads());
    }
    
    public HTTPChecker getChecker()
    {
        return this.checker;
    }
}
