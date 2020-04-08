package com.intrbiz.bergamot.model.message.processor.result.match;

import com.intrbiz.bergamot.model.message.MessageObject;
import com.intrbiz.bergamot.model.message.processor.ProcessorHashable;

/**
 * Criteria for matching passive results to checks
 */
public abstract class MatchOn extends MessageObject implements ProcessorHashable
{
    private static final long serialVersionUID = 1L;
    
    public MatchOn()
    {
        super();
    }
}
