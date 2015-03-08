package com.intrbiz.bergamot.worker.engine.agent;

import java.text.DecimalFormat;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.agent.server.BergamotAgentServerHandler;
import com.intrbiz.bergamot.model.message.agent.check.CheckDisk;
import com.intrbiz.bergamot.model.message.agent.stat.DiskStat;
import com.intrbiz.bergamot.model.message.agent.stat.disk.DiskInfo;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.ActiveResultMO;
import com.intrbiz.bergamot.model.message.result.ResultMO;
import com.intrbiz.bergamot.worker.engine.AbstractExecutor;

import static com.intrbiz.bergamot.util.UnitUtil.*;

/**
 * Check the cpu usage of a Bergamot Agent
 */
public class DiskExecutor extends AbstractExecutor<AgentEngine>
{
    public static final String NAME = "disk";
    
    private Logger logger = Logger.getLogger(DiskExecutor.class);
    
    private static final DecimalFormat DFMT = new DecimalFormat("0.00");

    public DiskExecutor()
    {
        super();
    }

    /**
     * Only execute Checks where the engine == "agent"
     */
    @Override
    public boolean accept(ExecuteCheck task)
    {
        return super.accept(task) && AgentEngine.NAME.equals(task.getEngine()) && NAME.equals(task.getExecutor());
    }

    @Override
    public void execute(ExecuteCheck executeCheck, Consumer<ResultMO> resultSubmitter)
    {
        logger.trace("Checking Bergamot Agent Disk Usage");
        try
        {
            // check the host presence
            UUID agentId = executeCheck.getUUIDParameter("agent_id");
            if (agentId == null) throw new RuntimeException("No agent_id parameter was given");
            String mount = executeCheck.getParameter("mount");
            if (Util.isEmpty(mount)) throw new RuntimeException("No disk mount point was given");
            // lookup the agent
            BergamotAgentServerHandler agent = this.getEngine().getAgentServer().getRegisteredAgent(agentId);
            if (agent != null)
            {
                // get the CPU stats
                agent.sendMessageToAgent(new CheckDisk(), (response) -> {
                    DiskStat stat = (DiskStat) response;
                    logger.trace("Got Disk usage: " + stat);
                    // find the mount
                    DiskInfo disk = stat.getDisks().stream().filter((di) -> { return mount.equals(di.getMount()); }).findFirst().orElse(null);
                    if (disk != null)
                    {
                        // apply the check
                        resultSubmitter.accept(new ActiveResultMO().fromCheck(executeCheck).applyThreshold(
                                fromPercent(disk.getUsedPercent()), 
                                executeCheck.getPercentParameter("warning", 0.8F),
                                executeCheck.getPercentParameter("critical", 0.9F),
                                "Disk: " + disk.getMount() + " " + disk.getType() + " on " + disk.getDevice() +" " + DFMT.format(toG(disk.getUsed())) + " GB of " + DFMT.format(toG(disk.getSize())) + " GB (" + DFMT.format(disk.getUsedPercent()) + " %) used" 
                        ));
                    }
                    else
                    {
                        resultSubmitter.accept(new ActiveResultMO().fromCheck(executeCheck).error("No such mount point: " + mount));
                    }
                });
            }
            else
            {
                // raise an error
                resultSubmitter.accept(new ActiveResultMO().fromCheck(executeCheck).error("Bergamot Agent disconnected"));
            }
        }
        catch (Exception e)
        {
            resultSubmitter.accept(new ActiveResultMO().fromCheck(executeCheck).error(e));
        }
    }
}
