package com.intrbiz.bergamot.worker.engine.nrpe;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer;
import com.intrbiz.Util;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.reading.ReadingParcelMO;
import com.intrbiz.bergamot.model.message.result.ActiveResultMO;
import com.intrbiz.bergamot.nagios.model.NagiosPerfData;
import com.intrbiz.bergamot.queue.key.ReadingKey;
import com.intrbiz.bergamot.worker.engine.AbstractExecutor;
import com.intrbiz.gerald.polyakov.Reading;
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
        return NRPEEngine.NAME.equalsIgnoreCase(task.getEngine());
    }
    
    @Override
    public void execute(ExecuteCheck executeCheck)
    {
        logger.info("Executing NRPE check : " + executeCheck.getEngine() + "::" + executeCheck.getName() + " for " + executeCheck.getCheckType() + " " + executeCheck.getCheckId());
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
                    ActiveResultMO resultMO = new ActiveResultMO().fromCheck(executeCheck);
                    resultMO.setOk(response.toOk());
                    resultMO.setStatus(response.toStatus());
                    resultMO.setOutput(response.getOutput());
                    resultMO.setRuntime(response.getRuntime());
                    tctx.stop();
                    this.publishActiveResult(executeCheck, resultMO);
                    // readings
                    ReadingParcelMO readings = new ReadingParcelMO().fromCheck(executeCheck.getCheckId());
                    for (NagiosPerfData perfData : response.getPerfData())
                    {
                        Reading reading = perfData.toReading();
                        if (reading != null) readings.reading(reading);
                    }
                    if (readings.getReadings().size() > 0) this.publishReading(new ReadingKey(executeCheck.getCheckId(), executeCheck.getProcessingPool()), readings);
                }, 
                (exception) -> {
                    tctx.stop();
                    failedRequests.inc();
                    this.publishActiveResult(executeCheck, new ActiveResultMO().fromCheck(executeCheck).error(exception));
                },
                command,
                args
            );
        }
        catch (Exception e)
        {
            logger.error("Failed to execute NRPE check", e);
            tctx.stop();
            this.failedRequests.inc();
            this.publishActiveResult(executeCheck, new ActiveResultMO().fromCheck(executeCheck).error(e));
        }        
    }
}
