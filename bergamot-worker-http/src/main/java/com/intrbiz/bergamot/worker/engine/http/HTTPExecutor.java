package com.intrbiz.bergamot.worker.engine.http;

import org.apache.log4j.Logger;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer;
import com.intrbiz.Util;
import com.intrbiz.bergamot.check.http.HTTPCheckBuilder;
import com.intrbiz.bergamot.check.http.HTTPCheckResponse;
import com.intrbiz.bergamot.model.message.ParameterMO;
import com.intrbiz.bergamot.model.message.processor.reading.ReadingParcelMessage;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.processor.result.ResultMessage;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.worker.engine.AbstractCheckExecutor;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;
import com.intrbiz.gerald.source.IntelligenceSource;
import com.intrbiz.gerald.witchcraft.Witchcraft;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;


/**
 * Execute HTTP checks
 */
public class HTTPExecutor extends AbstractCheckExecutor<HTTPEngine>
{
    public static final String NAME = "http";
    
    private Logger logger = Logger.getLogger(HTTPExecutor.class);
    
    private final Timer requestTimer;
    
    private final Counter failedRequests;
    
    public HTTPExecutor()
    {
        super(NAME, true);
        // setup metrics
        IntelligenceSource source = Witchcraft.get().source("com.intrbiz.bergamot.http");
        this.requestTimer = source.getRegistry().timer("all-http-requests");
        this.failedRequests = source.getRegistry().counter("failed-http-requests");
    }
    
    @Override
    public void execute(final ExecuteCheck executeCheck, final CheckExecutionContext context)
    {
        if (logger.isTraceEnabled()) logger.trace("Executing check : " + executeCheck.getEngine() + "::" + executeCheck.getExecutor() + "::" + executeCheck.getName() + " for " + executeCheck.getCheckType() + " " + executeCheck.getCheckId());
        // time it
        final Timer.Context tctx = this.requestTimer.time();
        try
        {
            if (Util.isEmpty(executeCheck.getParameter("host"))) throw new RuntimeException("The host must be defined!");
            //
            HTTPCheckBuilder check = this.getEngine().getChecker().check();
            // required parameters
            check.connect(executeCheck.getParameter("host"));
            // optional parameters
            // virtual host
            if (executeCheck.containsParameter("virtual_host")) check.host(executeCheck.getParameter("virtual_host", null));
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
                if (logger.isTraceEnabled()) logger.trace("Got response for HTTP check (" + executeCheck.getCheckId() + "/" + executeCheck.getId() + ")\n" + response);
                // compute the result
                ActiveResult resultMO = new ActiveResult().fromCheck(executeCheck);           
                // check the response
                boolean   cont = checkStatus(executeCheck, response, resultMO);
                if (cont) cont = checkRuntime(executeCheck, response, resultMO);
                if (cont) cont = checkLength(executeCheck, response, resultMO);
                if (cont) cont = checkContent(executeCheck, response, resultMO);
                // submit the result
                resultMO.setRuntime(response.getRuntime());
                tctx.stop();
                context.publishActiveResult(resultMO);
                // publish some metrics
                ReadingParcelMessage readings = new ReadingParcelMessage().fromCheck(executeCheck.getCheckId());
                readings.longGaugeReading("response-time", "ms", response.getRuntime(), (long) executeCheck.getIntParameter("warning_response_time", 0), (long) executeCheck.getIntParameter("critical_response_time", 0), null, null);
                readings.longGaugeReading("content-length", "B", (long) response.getResponse().content().capacity());
                readings.integerGaugeReading("status", null, response.getResponse().status().code());
                context.publishReading(readings);
            }, 
            (error) -> {
                tctx.stop();
                failedRequests.inc();
                if (logger.isTraceEnabled()) logger.trace("Error for HTTP check (" + executeCheck.getCheckId() + "/" + executeCheck.getId() + ")", error);
                context.publishActiveResult(new ActiveResult().fromCheck(executeCheck).error(error));
            });
        }
        catch (Exception e)
        {
            if (logger.isTraceEnabled()) logger.trace("Failed to execute HTTP check", e);
            tctx.stop();
            this.failedRequests.inc();
            context.publishActiveResult(new ActiveResult().fromCheck(executeCheck).error(e));
        }        
    }
    
    protected boolean checkStatus(ExecuteCheck check, HTTPCheckResponse response, ResultMessage resultMO)
    {
        if (check.getIntParameterCSV("status", "200, 301, 302, 304").stream().anyMatch((stat) -> { return response.getResponse().status().code() == stat; }))
        {
            resultMO.ok("Got " + response.getResponse().status() + ", " + response.getResponse().content().capacity() + " bytes, in " + response.getRuntime() + "ms");
            return true;
        }
        resultMO.critical("Got " + response.getResponse().status() + ", " + response.getResponse().content().capacity() + " bytes, in " + response.getRuntime() + "ms");
        return false;
    }
    
    protected boolean checkRuntime(ExecuteCheck check, HTTPCheckResponse response, ResultMessage resultMO)
    {
        if (check.containsParameter("critical_response_time"))
        {
            int criticalResponseTime = check.getIntParameter("critical_response_time", -1);
            if (criticalResponseTime > 0)
            {
                if (response.getRuntime() > criticalResponseTime)
                {
                    resultMO.critical("Got " + response.getResponse().status() + ", " + response.getResponse().content().capacity() + " bytes, in " + response.getRuntime() + "ms (slower than " + criticalResponseTime + "ms)");
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
                    resultMO.warning("Got " + response.getResponse().status() + ", " + response.getResponse().content().capacity() + " bytes, in " + response.getRuntime() + "ms (slower than " + warningResponseTime + "ms)");
                    return false;
                }
            }
        }
        return true;
    }
    
    protected boolean checkLength(ExecuteCheck check, HTTPCheckResponse response, ResultMessage resultMO)
    {
        if (check.containsParameter("length"))
        {
            int length = check.getIntParameter("length", -1);
            if (length > 0)
            {
                int contentLength = response.getResponse().content().capacity();
                if (contentLength < length)
                {
                    resultMO.critical("Got " + response.getResponse().status() + ", " + response.getResponse().content().capacity() + " bytes, in " + response.getRuntime() + "ms (content length " + contentLength + " < " + length + "')");
                    return false;
                }
            }
        }
        return true;
    }
    
    protected boolean checkContent(ExecuteCheck check, HTTPCheckResponse response, ResultMessage resultMO)
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
                resultMO.critical("Got " + response.getResponse().status() + ", " + response.getResponse().content().capacity() + " bytes, in " + response.getRuntime() + "ms (content does not contain: '" + contains + "')");
                return false;
            }
        }
        return true;
    }
}
