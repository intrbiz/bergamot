package com.intrbiz.bergamot.worker.engine.agent;

import java.util.UUID;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.agent.server.BergamotAgentServerHandler;
import com.intrbiz.bergamot.model.message.agent.hello.AgentHello;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.ActiveResultMO;
import com.intrbiz.bergamot.model.message.result.PassiveResultMO;
import com.intrbiz.bergamot.model.message.result.ResultMO;
import com.intrbiz.bergamot.queue.key.ResultKey;
import com.intrbiz.bergamot.worker.engine.AbstractExecutor;

/**
 * Check the presence of a Bergamot Agent
 */
public class PresenceExecutor extends AbstractExecutor<AgentEngine>
{
    public static final String NAME = "presence";
    
    private Logger logger = Logger.getLogger(PresenceExecutor.class);

    public PresenceExecutor()
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
        logger.debug("Checking Bergamot Agent presence");
        try
        {
            // get the agent id
            UUID agentId = executeCheck.getUUIDParameter("agent_id");
            if (agentId == null) throw new RuntimeException("No agent_id parameter was given");
            // check the host presence
            ResultMO resultMO = new ActiveResultMO().fromCheck(executeCheck);
            // lookup the agent
            BergamotAgentServerHandler agent = this.getEngine().getAgentServer().getRegisteredAgent(agentId);
            if (agent != null)
            {
                AgentHello hello = agent.getHello();
                String hostName = hello.getHostName();
                String tlsName  = agent.getClientCertificateInfo().getSubject().getCommonName();
                resultMO.ok("Bergamot Agent " + hostName + " (" + tlsName + ") connected");
            }
            else
            {
                resultMO.critical("Bergamot Agent disconnected");
            }
            // submit
            resultSubmitter.accept(resultMO);
        }
        catch (Exception e)
        {
            resultSubmitter.accept(new ActiveResultMO().fromCheck(executeCheck).error(e));
        }
    }
    
    @Override
    public void start()
    {
        // setup event handlers
        // on connection
        this.getEngine().getAgentServer().setOnAgentRegisterHandler((handler) -> {
            // info we need
            AgentHello hello = handler.getHello();
            UUID hostId = hello.getHostId();
            String hostName = hello.getHostName();
            String tlsName  = handler.getClientCertificateInfo().getSubject().getCommonName();
            // debug log
            logger.debug("Got agent connection: " + hostName + " " + hostId);
            // submit a passive result for the host
            this.publishResult(new ResultKey(hostId), new PassiveResultMO().passive(hostId).ok("Bergamot Agent " + hostName + " (" + tlsName + ") connected"));
        });
        // on disconnection
        this.getEngine().getAgentServer().setOnAgentUnregisterHandler((handler) -> {
            // info we need
            AgentHello hello = handler.getHello();
            UUID hostId = hello.getHostId();
            String hostName = hello.getHostName();
            // debug log
            logger.debug("Got agent disconnection: " + hostName + " " + hostId);
            // submit a passive result for the host
            this.publishResult(new ResultKey(hostId), new PassiveResultMO().passive(hostId).critical("Bergamot Agent disconnected"));
        });
    }
}
