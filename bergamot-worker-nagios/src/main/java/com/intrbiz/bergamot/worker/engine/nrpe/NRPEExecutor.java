package com.intrbiz.bergamot.worker.engine.nrpe;

import java.util.function.Consumer;

import org.apache.log4j.Logger;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer;
import com.intrbiz.Util;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.Result;
import com.intrbiz.bergamot.nagios.util.NagiosPluginParser;
import com.intrbiz.bergamot.worker.engine.AbstractExecutor;
import com.intrbiz.gerald.source.IntelligenceSource;
import com.intrbiz.gerald.witchcraft.Witchcraft;

/**
 * Execute NRPE checks from pure Java
 */
public class NRPEExecutor extends AbstractExecutor<NRPEEngine>
{
    private Logger logger = Logger.getLogger(NRPEExecutor.class);
    
    private final Timer nrpeRequestTimer;
    
    private final Counter failedRequests;
    
    public NRPEExecutor()
    {
        super();
        // setup metrics
        IntelligenceSource source = Witchcraft.get().source("com.intrbiz.bergamot.nrpe");
        this.nrpeRequestTimer = source.getRegistry().timer("all-nrpe-requests");
        this.failedRequests = source.getRegistry().counter("failed-nrpe-requests");
    }
    
    /**
     * Only execute Checks where the engine == "nrpe"
     */
    @Override
    public boolean accept(ExecuteCheck task)
    {
        return super.accept(task) && (task instanceof ExecuteCheck) && NRPEEngine.NAME.equals(((ExecuteCheck) task).getEngine());
    }
    
    @Override
    public void execute(ExecuteCheck executeCheck, Consumer<Result> resultSubmitter)
    {
        logger.info("Executing check : " + executeCheck.getEngine() + "::" + executeCheck.getName() + " for " + executeCheck.getCheckType() + " " + executeCheck.getCheckId());
        // time it
        Timer.Context tctx = this.nrpeRequestTimer.time();
        try
        {
            // the host
            String host = executeCheck.getParameter("host");
            if (Util.isEmpty(host)) throw new RuntimeException("The 'host' parameter must be provided.");
            // the command
            String command = executeCheck.getParameter("command");
            if (Util.isEmpty(command)) throw new RuntimeException("The 'command' parameter must be provided.");
            // submit the command to the poller
            // TODO timeouts
            this.getEngine().getPoller().command(
                host, 5666, 5,  60,  null, 
                (response, context) -> {
                    Result result = new Result().fromCheck(executeCheck);
                    NagiosPluginParser.parseNagiosExitCode(response.getResponseCode(), result);
                    NagiosPluginParser.parseNagiosOutput(response.getOutput(), result);
                    result.setRuntime(response.getRuntime());
                    tctx.stop();
                    resultSubmitter.accept(result);
                }, 
                (exception, context) -> {
                    tctx.stop();
                    failedRequests.inc();
                    resultSubmitter.accept(new Result().fromCheck(executeCheck).error(exception));
                },
                command
            );
        }
        catch (Exception e)
        {
            logger.error("Failed to execute NRPE check", e);
            tctx.stop();
            this.failedRequests.inc();
            resultSubmitter.accept(new Result().fromCheck(executeCheck).error(e));
        }        
    }
}
