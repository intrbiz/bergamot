package com.intrbiz.bergamot.worker.engine.nagios;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.codahale.metrics.Timer;
import com.intrbiz.Util;
import com.intrbiz.bergamot.model.message.processor.reading.ReadingParcelMessage;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.nagios.NagiosPluginExecutor;
import com.intrbiz.bergamot.nagios.model.NagiosPerfData;
import com.intrbiz.bergamot.nagios.model.NagiosResult;
import com.intrbiz.bergamot.worker.engine.AbstractCheckExecutor;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;
import com.intrbiz.gerald.polyakov.Reading;
import com.intrbiz.gerald.source.IntelligenceSource;
import com.intrbiz.gerald.witchcraft.Witchcraft;

/**
 * Execute Nagios Plugins (CHecks)
 * 
 * This CheckExecutor executes Nagios plugins (checks) by 
 * forking the check, just like Nagios would.
 * 
 * The exit code is read to determine the result status, 
 * and stdOut/stdErr are read to determine the check output.
 * 
 * Performance data is captured and stored as a parameter of 
 * the result.
 * 
 * Currently only the first line of the check output is read.
 * 
 * To avoid polluting the core of Bergamot with Nagios specific 
 * functionality, macro processing is pushed out to the edge.
 * As such the only component which needs to known about Nagios 
 * macros is this Nagios specific CheckExecutor implementation.
 * 
 */
public class NagiosExecutor extends AbstractCheckExecutor<NagiosEngine>
{
    private Logger logger = Logger.getLogger(NagiosExecutor.class);
    
    protected final Timer nagiosTimer;
    
    protected NagiosPluginExecutor executor;

    public NagiosExecutor()
    {
        super("nagios", true);
        // setup the executor
        this.executor = new NagiosPluginExecutor();
        // setup metrics
        IntelligenceSource source = Witchcraft.get().source("com.intrbiz.bergamot.nagios");
        this.nagiosTimer = source.getRegistry().timer("all-nagios-plugin-executions");
    }

    /**
     * Only execute Checks where the engine == "nagios"
     */
    @Override
    public boolean accept(ExecuteCheck task)
    {
        return NagiosEngine.NAME.equalsIgnoreCase(task.getEngine());
    }

    @Override
    public void execute(ExecuteCheck executeCheck, CheckExecutionContext context)
    {
        if (logger.isTraceEnabled()) logger.trace("Executing Nagios check : " + executeCheck.getEngine() + "::" + executeCheck.getName() + " for " + executeCheck.getCheckType() + " " + executeCheck.getCheckId());
        ActiveResult resultMO = new ActiveResult();
        ReadingParcelMessage readings = new ReadingParcelMessage().fromCheck(executeCheck.getCheckId());
        try
        {
            // validate the command line
            String commandLine = executeCheck.getParameter("command_line");
            if (Util.isEmpty(commandLine)) throw new RuntimeException("The command_line must be defined!");
            // execute
            Timer.Context tctx = this.nagiosTimer.time();
            try
            {
                NagiosResult response = this.executor.execute(commandLine);
                resultMO.setOk(response.toOk());
                resultMO.setStatus(response.toStatus());
                resultMO.setOutput(response.getOutput());
                // readings
                for (NagiosPerfData perfData : response.getPerfData())
                {
                    Reading reading = perfData.toReading();
                    if (reading != null) readings.reading(reading);
                }
            }
            finally
            {
                tctx.stop();
            }
        }
        catch (IOException | InterruptedException e)
        {
            if (logger.isTraceEnabled()) logger.trace("Failed to execute nagios check command", e);
            resultMO.error(e);
        }
        context.publishActiveResult(resultMO);
        if (readings != null && readings.getReadings().size() > 0) 
            context.publishReading(readings);
    }
}
