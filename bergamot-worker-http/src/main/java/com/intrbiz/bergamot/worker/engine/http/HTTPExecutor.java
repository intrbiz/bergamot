package com.intrbiz.bergamot.worker.engine.http;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import org.apache.log4j.Logger;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer;
import com.intrbiz.Util;
import com.intrbiz.bergamot.check.http.HTTPCheckBuilder;
import com.intrbiz.bergamot.check.http.HTTPCheckResponse;
import com.intrbiz.bergamot.model.message.ParameterMO;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.reading.ReadingParcelMO;
import com.intrbiz.bergamot.model.message.result.ActiveResultMO;
import com.intrbiz.bergamot.model.message.result.ResultMO;
import com.intrbiz.bergamot.queue.key.ReadingKey;
import com.intrbiz.bergamot.worker.engine.AbstractExecutor;
import com.intrbiz.gerald.source.IntelligenceSource;
import com.intrbiz.gerald.witchcraft.Witchcraft;


/**
 * Execute HTTP checks
 */
public class HTTPExecutor extends AbstractExecutor<HTTPEngine>
{
    public static final String NAME = "http";
    
    private Logger logger = Logger.getLogger(HTTPExecutor.class);
    
    private final Timer requestTimer;
    
    private final Counter failedRequests;
    
    public HTTPExecutor()
    {
        super();
        // setup metrics
        IntelligenceSource source = Witchcraft.get().source("com.intrbiz.bergamot.http");
        this.requestTimer = source.getRegistry().timer("all-http-requests");
        this.failedRequests = source.getRegistry().counter("failed-http-requests");
    }
    
    /**
     * Only execute Checks where the engine == "http" and (executor == "http" or executor == null)
     */
    @Override
    public boolean accept(ExecuteCheck task)
    {
        return HTTPEngine.NAME.equalsIgnoreCase(task.getEngine()) &&
               (HTTPExecutor.NAME.equalsIgnoreCase(task.getExecutor()) || Util.isEmpty(task.getExecutor()));
    }
    
    @Override
    public void execute(final ExecuteCheck executeCheck)
    {
        logger.info("Executing check : " + executeCheck.getEngine() + "::" + executeCheck.getExecutor() + "::" + executeCheck.getName() + " for " + executeCheck.getCheckType() + " " + executeCheck.getCheckId());
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
                logger.info("Got response for HTTP check (" + executeCheck.getCheckId() + "/" + executeCheck.getId() + ")\n" + response);
                // compute the result
                ActiveResultMO resultMO = new ActiveResultMO().fromCheck(executeCheck);           
                // check the response
                boolean   cont = checkStatus(executeCheck, response, resultMO);
                if (cont) cont = checkRuntime(executeCheck, response, resultMO);
                if (cont) cont = checkLength(executeCheck, response, resultMO);
                if (cont) cont = checkContent(executeCheck, response, resultMO);
                // submit the result
                resultMO.setRuntime(response.getRuntime());
                tctx.stop();
                this.publishActiveResult(executeCheck, resultMO);
                // publish some metrics
                ReadingParcelMO readings = new ReadingParcelMO().fromCheck(executeCheck.getCheckId());
                readings.longGaugeReading("response-time", "ms", response.getRuntime(), (long) executeCheck.getIntParameter("warning_response_time", 0), (long) executeCheck.getIntParameter("critical_response_time", 0), null, null);
                readings.longGaugeReading("content-length", "B", (long) response.getResponse().content().capacity());
                readings.integerGaugeReading("status", null, response.getResponse().getStatus().code());
                this.publishReading(new ReadingKey(executeCheck.getCheckId(), executeCheck.getProcessingPool()), readings);
            }, 
            (error) -> {
                tctx.stop();
                failedRequests.inc();
                logger.error("Error for HTTP check (" + executeCheck.getCheckId() + "/" + executeCheck.getId() + ")", error);
                this.publishActiveResult(executeCheck, new ActiveResultMO().fromCheck(executeCheck).error(error));
            });
        }
        catch (Exception e)
        {
            logger.error("Failed to execute HTTP check", e);
            tctx.stop();
            this.failedRequests.inc();
            this.publishActiveResult(executeCheck, new ActiveResultMO().fromCheck(executeCheck).error(e));
        }        
    }
    
    protected boolean checkStatus(ExecuteCheck check, HTTPCheckResponse response, ResultMO resultMO)
    {
        if (check.getIntParameterCSV("status", "200, 301, 302, 304").stream().anyMatch((stat) -> { return response.getResponse().getStatus().code() == stat; }))
        {
            resultMO.ok("Got " + response.getResponse().getStatus() + ", " + response.getResponse().content().capacity() + " bytes, in " + response.getRuntime() + "ms");
            return true;
        }
        resultMO.critical("Got " + response.getResponse().getStatus() + ", " + response.getResponse().content().capacity() + " bytes, in " + response.getRuntime() + "ms");
        return false;
    }
    
    protected boolean checkRuntime(ExecuteCheck check, HTTPCheckResponse response, ResultMO resultMO)
    {
        if (check.containsParameter("critical_response_time"))
        {
            int criticalResponseTime = check.getIntParameter("critical_response_time", -1);
            if (criticalResponseTime > 0)
            {
                if (response.getRuntime() > criticalResponseTime)
                {
                    resultMO.critical("Got " + response.getResponse().getStatus() + ", " + response.getResponse().content().capacity() + " bytes, in " + response.getRuntime() + "ms (slower than " + criticalResponseTime + "ms)");
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
                    resultMO.warning("Got " + response.getResponse().getStatus() + ", " + response.getResponse().content().capacity() + " bytes, in " + response.getRuntime() + "ms (slower than " + warningResponseTime + "ms)");
                    return false;
                }
            }
        }
        return true;
    }
    
    protected boolean checkLength(ExecuteCheck check, HTTPCheckResponse response, ResultMO resultMO)
    {
        if (check.containsParameter("length"))
        {
            int length = check.getIntParameter("length", -1);
            if (length > 0)
            {
                int contentLength = response.getResponse().content().capacity();
                if (contentLength < length)
                {
                    resultMO.critical("Got " + response.getResponse().getStatus() + ", " + response.getResponse().content().capacity() + " bytes, in " + response.getRuntime() + "ms (content length " + contentLength + " < " + length + "')");
                    return false;
                }
            }
        }
        return true;
    }
    
    protected boolean checkContent(ExecuteCheck check, HTTPCheckResponse response, ResultMO resultMO)
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
                resultMO.critical("Got " + response.getResponse().getStatus() + ", " + response.getResponse().content().capacity() + " bytes, in " + response.getRuntime() + "ms (content does not contain: '" + contains + "')");
                return false;
            }
        }
        return true;
    }
}
