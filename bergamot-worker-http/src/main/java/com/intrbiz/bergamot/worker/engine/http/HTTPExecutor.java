package com.intrbiz.bergamot.worker.engine.http;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import java.util.function.Consumer;

import org.apache.log4j.Logger;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer;
import com.intrbiz.Util;
import com.intrbiz.bergamot.model.message.ParameterMO;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.Result;
import com.intrbiz.bergamot.worker.check.http.HTTPCheckBuilder;
import com.intrbiz.bergamot.worker.check.http.HTTPCheckResponse;
import com.intrbiz.bergamot.worker.engine.AbstractExecutor;
import com.intrbiz.gerald.source.IntelligenceSource;
import com.intrbiz.gerald.witchcraft.Witchcraft;

/**
 * Execute HTTP checks
 */
public class HTTPExecutor extends AbstractExecutor<HTTPEngine>
{
    private Logger logger = Logger.getLogger(HTTPExecutor.class);
    
    private final Timer httpRequestTimer;
    
    private final Counter failedRequests;
    
    public HTTPExecutor()
    {
        super();
        // setup metrics
        IntelligenceSource source = Witchcraft.get().source("com.intrbiz.bergamot.http");
        this.httpRequestTimer = source.getRegistry().timer("all-http-requests");
        this.failedRequests = source.getRegistry().counter("failed-http-requests");
    }
    
    /**
     * Only execute Checks where the engine == "http"
     */
    @Override
    public boolean accept(ExecuteCheck task)
    {
        return super.accept(task) && (task instanceof ExecuteCheck) && HTTPEngine.NAME.equals(((ExecuteCheck) task).getEngine());
    }
    
    @Override
    public void execute(final ExecuteCheck executeCheck, Consumer<Result> resultSubmitter)
    {
        logger.info("Executing check : " + executeCheck.getEngine() + "::" + executeCheck.getName() + " for " + executeCheck.getCheckType() + " " + executeCheck.getCheckId());
        // time it
        final Timer.Context tctx = this.httpRequestTimer.time();
        try
        {
            if (Util.isEmpty(executeCheck.getParameter("host"))) throw new RuntimeException("The host must be defined!");
            //
            HTTPCheckBuilder check = this.getEngine().getChecker().check();
            // required parameters
            check.connect(executeCheck.getParameter("host"));
            // optional parameters
            // virtual host
            if (executeCheck.containsParameter("virtual_host")) check.path(executeCheck.getParameter("virtual_host", null));
            // port number
            if (executeCheck.containsParameter("port")) check.port(executeCheck.getIntParameter("port", -1));
            // ssl
            if (executeCheck.containsParameter("ssl")) check.ssl(executeCheck.getBooleanParameter("ssl", false));
            if (executeCheck.containsParameter("permit_invalid_certs")) check.permitInvalidCerts(executeCheck.getBooleanParameter("permit_invalid_certs", false));
            // the request
            if (executeCheck.containsParameter("version")) check.version(HttpVersion.valueOf(executeCheck.getParameter("version", "HTTP/1.1").toUpperCase()));
            if (executeCheck.containsParameter("method")) check.method(HttpMethod.valueOf(executeCheck.getParameter("method", "GET").toUpperCase()));
            if (executeCheck.containsParameter("path")) check.path(executeCheck.getParameter("path", "/"));
            // any headers
            for (ParameterMO header : executeCheck.getParametersStartingWith("header:"))
            {
                check.header(header.getName().substring("header:".length() + 1), header.getValue());
            }
            // execute
            check.execute((response) -> {
                // compute the result
                Result result = new Result().fromCheck(executeCheck);           
                // check the response
                boolean   cont = checkStatus(executeCheck, response, result);
                if (cont) cont = checkRuntime(executeCheck, response, result);
                if (cont) cont = checkLength(executeCheck, response, result);
                if (cont) cont = checkContent(executeCheck, response, result);
                // submit the result
                result.setRuntime(response.getRuntime());
                tctx.stop();
                resultSubmitter.accept(result); 
            }, 
            (error) -> {
                tctx.stop();
                failedRequests.inc();
                resultSubmitter.accept(new Result().fromCheck(executeCheck).error(error));
            });
        }
        catch (Exception e)
        {
            logger.error("Failed to execute HTTP check", e);
            tctx.stop();
            this.failedRequests.inc();
            resultSubmitter.accept(new Result().fromCheck(executeCheck).error(e));
        }        
    }
    
    protected boolean checkStatus(ExecuteCheck check, HTTPCheckResponse response, Result result)
    {
        if (check.getIntParameterCSV("status", "200, 301, 302, 304").stream().anyMatch((stat) -> { return response.getResponse().getStatus().code() == stat; }))
        {
            result.ok("Got " + response.getResponse().getStatus() + ", " + response.getResponse().content().capacity() + " bytes, in " + response.getRuntime() + "ms");
            return true;
        }
        result.critical("Got " + response.getResponse().getStatus() + ", " + response.getResponse().content().capacity() + " bytes, in " + response.getRuntime() + "ms");
        return false;
    }
    
    protected boolean checkRuntime(ExecuteCheck check, HTTPCheckResponse response, Result result)
    {
        if (check.containsParameter("critical_response_time"))
        {
            int criticalResponseTime = check.getIntParameter("critical_response_time", -1);
            if (criticalResponseTime > 0)
            {
                if (response.getRuntime() > criticalResponseTime)
                {
                    result.critical("Got " + response.getResponse().getStatus() + ", " + response.getResponse().content().capacity() + " bytes, in " + response.getRuntime() + "ms (slower than " + criticalResponseTime + "ms)");
                    return false;
                }
            }
        }
        else if (check.containsParameter("warning_response_time"))
        {
            int warningResponseTime = check.getIntParameter("warning_response_time", -1);
            if (warningResponseTime > 0)
            {
                if (response.getRuntime() > warningResponseTime)
                {
                    result.warning("Got " + response.getResponse().getStatus() + ", " + response.getResponse().content().capacity() + " bytes, in " + response.getRuntime() + "ms (slower than " + warningResponseTime + "ms)");
                    return false;
                }
            }
        }
        return true;
    }
    
    protected boolean checkLength(ExecuteCheck check, HTTPCheckResponse response, Result result)
    {
        if (check.containsParameter("length"))
        {
            int length = check.getIntParameter("length", -1);
            if (length > 0)
            {
                int contentLength = response.getResponse().content().capacity();
                if (contentLength < length)
                {
                    result.critical("Got " + response.getResponse().getStatus() + ", " + response.getResponse().content().capacity() + " bytes, in " + response.getRuntime() + "ms (content length " + contentLength + " < " + length + "')");
                    return false;
                }
            }
        }
        return true;
    }
    
    protected boolean checkContent(ExecuteCheck check, HTTPCheckResponse response, Result result)
    {
        if (check.containsParameter("contains"))
        {
            String contains = check.getParameter("contains");
            // get the content as a string
            // TODO: we should detect the charset rather than assuming UTF8
            String content = response.getResponse().content().toString(Util.UTF8);
            // check we contain the given string
            if (! content.contains(contains))
            {
                result.critical("Got " + response.getResponse().getStatus() + ", " + response.getResponse().content().capacity() + " bytes, in " + response.getRuntime() + "ms (content does not contain: '" + contains + "')");
                return false;
            }
        }
        return true;
    }
}
