package com.intrbiz.bergamot.model.message.api.update;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.api.APIRequest;

@JsonTypeName("bergamot.api.register_for_updates")
public class RegisterForUpdates extends APIRequest
{
    @JsonProperty("update_type")
    private String updateType;
    
    @JsonProperty("ids")
    private List<UUID> ids = new LinkedList<UUID>();

    public RegisterForUpdates()
    {
        super();
    }

    public String getUpdateType()
    {
        return updateType;
    }

    public void setUpdateType(String updateType)
    {
        this.updateType = updateType;
    }

    public List<UUID> getIds()
    {
        return ids;
    }

    public void setIds(List<UUID> ids)
    {
        this.ids = ids;
    }
}
