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
    @JsonProperty("check_ids")
    private List<UUID> checkIds = new LinkedList<UUID>();

    public RegisterForUpdates()
    {
        super();
    }

    public List<UUID> getCheckIds()
    {
        return checkIds;
    }

    public void setCheckIds(List<UUID> checkIds)
    {
        this.checkIds = checkIds;
    }
}
