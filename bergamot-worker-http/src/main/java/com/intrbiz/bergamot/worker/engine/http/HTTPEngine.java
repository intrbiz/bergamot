package com.intrbiz.bergamot.worker.engine.http;

import com.intrbiz.bergamot.BergamotVersion;
import com.intrbiz.bergamot.check.http.HTTPChecker;
import com.intrbiz.bergamot.worker.engine.AbstractCheckEngine;

/**
 * A dedicated HTTP check engine
 */
public class HTTPEngine extends AbstractCheckEngine
{
    public static final String NAME = "http";
    
    private HTTPChecker checker;

    public HTTPEngine()
    {
        super(BergamotVersion.NAME, NAME, true, 
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
