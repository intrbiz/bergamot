package com.intrbiz.bergamot.result;

import com.intrbiz.bergamot.model.message.result.ResultMO;

public interface ResultConsumer
{
    
    ResultMO pollResult();

}
