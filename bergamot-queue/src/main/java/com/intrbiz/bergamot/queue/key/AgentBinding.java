package com.intrbiz.bergamot.queue.key;

import java.util.UUID;

public class AgentBinding extends WorkerKey
{    
    public AgentBinding(UUID agent)
    {
        super("*.*.*." + agent);
    }
}
