package com.intrbiz.bergamot.model.message.api.update;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.api.APIEvent;
import com.intrbiz.bergamot.model.message.event.update.Update;

@JsonTypeName("bergamot.api.event.update")
public class UpdateEvent extends APIEvent
{
    @JsonProperty("update")
    private Update update;

    public UpdateEvent()
    {
        super();
    }

    public UpdateEvent(Update update)
    {
        super();
        this.update = update;
    }

    public Update getUpdate()
    {
        return update;
    }

    public void setUpdate(Update update)
    {
        this.update = update;
    }
}
