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
        super(NAME, 
                new HTTPExecutor(),
                new CertificateExecutor(),
                new ScriptedHTTPExecutor());
        this.checker = new HTTPChecker(Runtime.getRuntime().availableProcessors());
    }
    
    public HTTPChecker getChecker()
    {
        return this.checker;
    }
}
