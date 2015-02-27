package com.intrbiz.bergamot.worker.engine.agent;

import java.util.function.Consumer;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.Result;
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
    public void execute(ExecuteCheck executeCheck, Consumer<Result> resultSubmitter)
    {
        logger.debug("Checking Bergamot Agent presence");
    }
    
    @Override
    public void start()
    {
        // setup event handlers
        this.getEngine().getAgentServer().setOnAgentRegisterHandler((handler)->{
            logger.debug("Got agent connection: " + handler.getHello().getAgentName());
        });
    }
}
