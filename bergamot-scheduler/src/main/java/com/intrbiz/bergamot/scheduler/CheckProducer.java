package com.intrbiz.bergamot.scheduler;

import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.ResultMO;

public interface CheckProducer
{
    public static enum PublishStatus
    {
        Success,
        Failed,
        Unroutable
    }
    
    PublishStatus publishCheck(ExecuteCheck check);
    
    boolean publishFailedCheck(ResultMO result);

}
