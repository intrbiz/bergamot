package com.intrbiz.bergamot.worker.engine.ssh;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.check.ssh.ExecStat;
import com.intrbiz.bergamot.check.ssh.SSHCheckContext;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.processor.reading.ReadingParcelMO;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.nagios.model.NagiosPerfData;
import com.intrbiz.bergamot.nagios.model.NagiosResult;
import com.intrbiz.bergamot.worker.engine.AbstractExecutor;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;
import com.intrbiz.bergamot.worker.engine.ssh.util.SSHCheckUtil;
import com.intrbiz.gerald.polyakov.Reading;

/**
 * Execute checks over SSH
 */
public class NagiosSSHExecutor extends AbstractExecutor<SSHEngine>
{
    public static final String NAME = "nagios";
    
    private Logger logger = Logger.getLogger(NagiosSSHExecutor.class);

    public NagiosSSHExecutor()
    {
        super();
    }

    /**
     * Only execute Checks where the engine == "ssh" and executor == "nagios"
     */
    @Override
    public boolean accept(ExecuteCheck task)
    {
        return SSHEngine.NAME.equalsIgnoreCase(task.getEngine()) && 
               NagiosSSHExecutor.NAME.equalsIgnoreCase(task.getExecutor());
    }

    @Override
    public void execute(final ExecuteCheck executeCheck, final CheckExecutionContext checkContext)
    {
        try
        {
            // validate the task
            SSHCheckUtil.validateSSHParameters(executeCheck);
            if (Util.isEmpty(executeCheck.getParameter("command_line"))) throw new RuntimeException("The 'command_line' parameter must be given");
            // create our context
            SSHCheckContext context = this.getEngine().getChecker().createContext((e) -> { 
                checkContext.publishActiveResult(new ActiveResult().fromCheck(executeCheck).error(e)); 
             });
            // setup the context - this will add SSH keys etc
            SSHCheckUtil.setupSSHCheckContext(executeCheck, context);
            // connect to the host
            context.connect(SSHCheckUtil.getSSHUsername(executeCheck), SSHCheckUtil.getSSHHost(executeCheck), SSHCheckUtil.getSSHPort(executeCheck), (session) -> {
                // the command to execute
                String command = executeCheck.getParameter("command_line");
                logger.debug("Executing Nagios plugin via SSH, command: " + command);
                // execute the command
                long start = System.currentTimeMillis();
                ExecStat stat = session.exec(command);
                long runtime = System.currentTimeMillis() - start;
                // parse the nagios result
                NagiosResult response = new NagiosResult(stat.getStdOut(), stat.getExit(), runtime);
                // publish the result
                ActiveResult resultMO = new ActiveResult().fromCheck(executeCheck);
                resultMO.setOk(response.toOk());
                resultMO.setStatus(response.toStatus());
                resultMO.setOutput(response.getOutput());
                resultMO.setRuntime(response.getRuntime());
                checkContext.publishActiveResult(resultMO);
                // readings
                if (! response.getPerfData().isEmpty())
                {
                    ReadingParcelMO readings = new ReadingParcelMO().fromCheck(executeCheck.getCheckId());
                    for (NagiosPerfData perfData : response.getPerfData())
                    {
                        Reading reading = perfData.toReading();
                        if (reading != null) readings.reading(reading);
                    }
                    if (readings != null && readings.getReadings().size() > 0)
                        checkContext.publishReading(readings);
                }
            });
        }
        catch (Exception e)
        {
            logger.error("Failed to execute Nagios over SSH check", e);
            checkContext.publishActiveResult(new ActiveResult().fromCheck(executeCheck).error(e));
        }
    }
}
