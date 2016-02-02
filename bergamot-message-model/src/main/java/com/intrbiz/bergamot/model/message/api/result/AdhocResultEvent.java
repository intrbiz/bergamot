package com.intrbiz.bergamot.model.message.api.result;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.api.APIEvent;
import com.intrbiz.bergamot.model.message.result.ResultMO;

@JsonTypeName("bergamot.api.event.adhoc_result")
public class AdhocResultEvent extends APIEvent
{
    @JsonProperty("result")
    private ResultMO result;

    public AdhocResultEvent()
    {
        super();
    }

    public AdhocResultEvent(ResultMO result)
    {
        super();
        this.result = result;
    }

    public ResultMO getResult()
    {
        return result;
    }

    public void setResult(ResultMO result)
    {
        this.result = result;
    }
}
