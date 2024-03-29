package com.intrbiz.bergamot.worker.engine.nrpe;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer;
import com.intrbiz.Util;
import com.intrbiz.bergamot.model.message.processor.reading.ReadingParcelMessage;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.nagios.model.NagiosPerfData;
import com.intrbiz.bergamot.worker.engine.AbstractCheckExecutor;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;
import com.intrbiz.gerald.polyakov.Reading;
import com.intrbiz.gerald.source.IntelligenceSource;
import com.intrbiz.gerald.witchcraft.Witchcraft;

/**
 * Execute NRPE checks from pure Java
 */
public class NRPEExecutor extends AbstractCheckExecutor<NRPEEngine>
{
    private static final Logger logger = Logger.getLogger(NRPEExecutor.class);
    
    private final Timer nrpeRequestTimer;
    
    private final Counter failedRequests;
    
    public NRPEExecutor()
    {
        super("nrpe", true);
        // setup metrics
        IntelligenceSource source = Witchcraft.get().source("com.intrbiz.bergamot.nrpe");
        this.nrpeRequestTimer = source.getRegistry().timer("all-nrpe-requests");
        this.failedRequests = source.getRegistry().counter("failed-nrpe-requests");
    }
    
    @Override
    public void execute(ExecuteCheck executeCheck, CheckExecutionContext context)
    {
        if (logger.isTraceEnabled()) logger.trace("Executing NRPE check : " + executeCheck.getEngine() + "::" + executeCheck.getName() + " for " + executeCheck.getCheckType() + " " + executeCheck.getCheckId());
        Timer.Context tctx = this.nrpeRequestTimer.time();
        try
        {
            // the host
            String host = executeCheck.getParameter("host");
            if (Util.isEmpty(host)) throw new RuntimeException("The 'host' parameter must be provided.");
            // the command
            String command = executeCheck.getParameter("command");
            if (Util.isEmpty(command)) throw new RuntimeException("The 'command' parameter must be provided.");
            // arguments - any parameter starting with 'arg' is treated as an argument to send to NRPE
            List<String> args = executeCheck.getParametersStartingWith("nrpe-arg")
                    .stream()
                    .sorted((a,b) -> a.getName().compareTo(b.getName()))
                    .map((p) -> p.getValue())
                    .collect(Collectors.toList());
            if (logger.isTraceEnabled()) logger.trace("Sending arguments: " + args);
            // submit the command to the poller
            // TODO timeouts
            this.getEngine().getPoller().command(
                host, 5666, 5,  60, 
                (response) -> {
                    ActiveResult resultMO = new ActiveResult();
                    resultMO.setOk(response.toOk());
                    resultMO.setStatus(response.toStatus());
                    resultMO.setOutput(response.getOutput());
                    tctx.stop();
                    context.publishActiveResult(resultMO);
                    // readings
                    ReadingParcelMessage readings = new ReadingParcelMessage().fromCheck(executeCheck.getCheckId());
                    for (NagiosPerfData perfData : response.getPerfData())
                    {
                        Reading reading = perfData.toReading();
                        if (reading != null) readings.reading(reading);
                    }
                    if (readings.getReadings().size() > 0) context.publishReading(readings);
                }, 
                (exception) -> {
                    tctx.stop();
                    failedRequests.inc();
                    context.publishActiveResult(new ActiveResult().error(exception));
                },
                command,
                args
            );
        }
        catch (Exception e)
        {
            if (logger.isTraceEnabled()) logger.trace("Failed to execute NRPE check", e);
            tctx.stop();
            this.failedRequests.inc();
            context.publishActiveResult(new ActiveResult().error(e));
        }        
    }
}
