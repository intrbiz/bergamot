package com.intrbiz.bergamot.result;

import com.intrbiz.bergamot.model.message.processor.result.ResultMessage;


/**
 * Process the result queue and update the object states
 */
public interface ResultProcessor
{
    void start();
    
    void shutdown();
    
    /**
     * Process the result of a check which executed
     * @param resultMO
     */
    void process(ResultMessage resultMO);
}
