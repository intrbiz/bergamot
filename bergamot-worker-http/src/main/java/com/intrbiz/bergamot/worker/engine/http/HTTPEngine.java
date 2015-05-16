package com.intrbiz.bergamot.worker.engine.http;

import com.intrbiz.bergamot.check.http.HTTPChecker;
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
            this.addExecutor(new CertificateExecutor());
            this.addExecutor(new ScriptedHTTPExecutor());
        }
        // setup our checker
        this.checker = new HTTPChecker(this.getWorker() != null && this.getWorker().getConfiguration() != null ? this.getWorker().getConfiguration().getThreads() : ((Runtime.getRuntime().availableProcessors() * 2) + 4));
    }
    
    public HTTPChecker getChecker()
    {
        return this.checker;
    }
}
