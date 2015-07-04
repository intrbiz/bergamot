package com.intrbiz.bergamot.worker.engine.nagios;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.codahale.metrics.Timer;
import com.intrbiz.Util;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.reading.ReadingParcelMO;
import com.intrbiz.bergamot.model.message.result.ActiveResultMO;
import com.intrbiz.bergamot.nagios.NagiosPluginExecutor;
import com.intrbiz.bergamot.nagios.model.NagiosPerfData;
import com.intrbiz.bergamot.nagios.model.NagiosResult;
import com.intrbiz.bergamot.queue.key.ReadingKey;
import com.intrbiz.bergamot.worker.engine.AbstractExecutor;
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
public class NagiosExecutor extends AbstractExecutor<NagiosEngine>
{
    private Logger logger = Logger.getLogger(NagiosExecutor.class);
    
    protected final Timer nagiosTimer;
    
    protected NagiosPluginExecutor executor;

    public NagiosExecutor()
    {
        super();
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
    public void execute(ExecuteCheck executeCheck)
    {
        logger.debug("Executing Nagios check : " + executeCheck.getEngine() + "::" + executeCheck.getName() + " for " + executeCheck.getCheckType() + " " + executeCheck.getCheckId());
        ActiveResultMO resultMO = new ActiveResultMO().fromCheck(executeCheck);
        ReadingParcelMO readings = new ReadingParcelMO().fromCheck(executeCheck.getCheckId());
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
                resultMO.setRuntime(response.getRuntime());
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
            logger.error("Failed to execute nagios check command", e);
            resultMO.error(e);
        }
        this.publishActiveResult(executeCheck, resultMO);
        if (readings != null && readings.getReadings().size() > 0) this.publishReading(new ReadingKey(executeCheck.getCheckId(), executeCheck.getProcessingPool()), readings);
    }
}
