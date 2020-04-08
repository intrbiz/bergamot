package com.intrbiz.bergamot.model.message.api.result;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.api.APIEvent;
import com.intrbiz.bergamot.model.message.processor.result.ResultMessage;

@JsonTypeName("bergamot.api.event.adhoc_result")
public class AdhocResultEvent extends APIEvent
{
    @JsonProperty("result")
    private ResultMessage result;

    public AdhocResultEvent()
    {
        super();
    }

    public AdhocResultEvent(ResultMessage result)
    {
        super();
        this.result = result;
    }

    public ResultMessage getResult()
    {
        return result;
    }

    public void setResult(ResultMessage result)
    {
        this.result = result;
    }
}
