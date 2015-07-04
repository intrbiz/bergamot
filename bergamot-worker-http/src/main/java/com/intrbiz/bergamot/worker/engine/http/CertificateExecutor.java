package com.intrbiz.bergamot.worker.engine.http;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import org.apache.log4j.Logger;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer;
import com.intrbiz.Util;
import com.intrbiz.bergamot.check.http.HTTPCheckBuilder;
import com.intrbiz.bergamot.crypto.util.CertInfo;
import com.intrbiz.bergamot.crypto.util.TLSInfo;
import com.intrbiz.bergamot.model.message.ParameterMO;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.ActiveResultMO;
import com.intrbiz.bergamot.worker.engine.AbstractExecutor;
import com.intrbiz.gerald.source.IntelligenceSource;
import com.intrbiz.gerald.witchcraft.Witchcraft;

/**
 * Execute TLS Certificate checks
 */
public class CertificateExecutor extends AbstractExecutor<HTTPEngine>
{
    public static final String NAME = "certificate";
    
    private Logger logger = Logger.getLogger(CertificateExecutor.class);
    
    private final Timer requestTimer;
    
    private final Counter failedRequests;
    
    public CertificateExecutor()
    {
        super();
        // setup metrics
        IntelligenceSource source = Witchcraft.get().source("com.intrbiz.bergamot.http");
        this.requestTimer = source.getRegistry().timer("all-http-requests");
        this.failedRequests = source.getRegistry().counter("failed-http-requests");
    }
    
    /**
     * Only execute Checks where the engine == "http" and executor = "certificate"
     */
    @Override
    public boolean accept(ExecuteCheck task)
    {
        return HTTPEngine.NAME.equalsIgnoreCase(task.getEngine()) &&
               CertificateExecutor.NAME.equalsIgnoreCase(task.getExecutor());
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
            // force ssl
            check.ssl();
            // allow bad certs, so we can still check
            check.permitInvalidCerts();
            // required parameters
            check.connect(executeCheck.getParameter("host"));
            // optional parameters
            // virtual host
            if (executeCheck.containsParameter("virtual_host")) check.host(executeCheck.getParameter("virtual_host", null));
            // port number
            if (executeCheck.containsParameter("port")) check.port(executeCheck.getIntParameter("port", -1));
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
                logger.info("Got response for TLS Certificate check (" + executeCheck.getCheckId() + "/" + executeCheck.getId() + ")\n" + response);
                // compute the result
                ActiveResultMO resultMO = new ActiveResultMO().fromCheck(executeCheck);
                // check the response
                TLSInfo tls = response.getTlsInfo();
                CertInfo serverCert = tls.getServerCertInfo();
                // first, is the certificate valid?
                if (tls.isValidCertificate())
                {
                    // check the expiry date, if it is close raise an error
                    long tillExpiry = (serverCert.getNotAfter().getTime() - System.currentTimeMillis()) / (1000 * 60 * 60 * 24); /* days */
                    // check how long till the certificate expires
                    if (tillExpiry <= executeCheck.getIntParameter("expires_in", 28))
                    {
                        resultMO.action("TLS certificate is valid but expires in " + tillExpiry + " days!");
                    }
                    else
                    {
                        resultMO.ok("TLS certificate is valid and expires in " + tillExpiry + " days");
                    }
                }
                else
                {
                    // well, little point in going further, the certificate is crap
                    resultMO.critical("TLS certificate is not valid: " + Util.coalesceEmpty(Util.nullable(tls.getCertificateValidationError(), Throwable::getMessage), " for some reason") + ".");
                }
                // submit the result
                resultMO.setRuntime(response.getRuntime());
                tctx.stop();
                this.publishActiveResult(executeCheck, resultMO); 
            }, 
            (error) -> {
                tctx.stop();
                failedRequests.inc();
                logger.error("Error for TLS Certificate check (" + executeCheck.getCheckId() + "/" + executeCheck.getId() + ")", error);
                this.publishActiveResult(executeCheck, new ActiveResultMO().fromCheck(executeCheck).error(error));
            });
        }
        catch (Exception e)
        {
            logger.error("Failed to execute TLS Certificate check", e);
            tctx.stop();
            this.failedRequests.inc();
            this.publishActiveResult(executeCheck, new ActiveResultMO().fromCheck(executeCheck).error(e));
        }        
    }

}
