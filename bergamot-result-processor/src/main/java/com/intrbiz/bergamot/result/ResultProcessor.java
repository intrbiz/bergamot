package com.intrbiz.bergamot.result;

import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.ResultMO;


/**
 * Process the result queue and update the object states
 */
public interface ResultProcessor
{
    
    void start();
    
    /**
     * Process the result of a check which executed
     * @param resultMO
     */
    void processExecuted(ResultMO resultMO);
    
    /**
     * Process a check execution which has died, did not 
     * get executed within its TTL.
     * @param check
     */
    void processDead(ExecuteCheck check);
    
    /**
     * Process a check execution which was aborted as the 
     * agent is not connected.
     * @param check
     */
    void processDeadAgent(ExecuteCheck check);
}
