package com.intrbiz.bergamot.model.message.event.update;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.GroupMO;

/**
 * A group state update
 */
@JsonTypeName("bergamot.group_update")
public class GroupUpdate extends Update
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("group")
    private GroupMO group;

    public GroupUpdate()
    {
        super();
    }
    
    public GroupUpdate(GroupMO group)
    {
        super(System.currentTimeMillis());
        this.group = group;
    }

    public GroupMO getGroup()
    {
        return group;
    }

    public void setGroup(GroupMO group)
    {
        this.group = group;
    }
}
