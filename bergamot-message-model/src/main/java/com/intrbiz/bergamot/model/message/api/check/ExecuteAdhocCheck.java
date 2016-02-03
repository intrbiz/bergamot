package com.intrbiz.bergamot.model.message.api.check;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.api.APIRequest;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;

@JsonTypeName("bergamot.api.bergamot.api.execute_adhoc_check")
public class ExecuteAdhocCheck extends APIRequest
{
    private ExecuteCheck check;
    
    public ExecuteAdhocCheck()
    {
        super();
    }
    
    public ExecuteAdhocCheck(ExecuteCheck check)
    {
        super();
        this.check = check;
    }

    public ExecuteCheck getCheck()
    {
        return check;
    }

    public void setCheck(ExecuteCheck check)
    {
        this.check = check;
    }
}
