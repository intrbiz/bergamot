package com.intrbiz.bergamot.model.message;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.state.GroupStateMO;

/**
 * Contact message object
 */
@JsonTypeName("bergamot.group")
public class GroupMO extends NamedObjectMO
{
    @JsonProperty("state")
    private GroupStateMO state;
    
    @JsonProperty("parents")
    private List<GroupMO> parents = new LinkedList<GroupMO>();
    
    @JsonProperty("children")
    private List<GroupMO> children = new LinkedList<GroupMO>();
    
    @JsonProperty("checks")
    private List<CheckMO> checks = new LinkedList<CheckMO>();
    
    public GroupMO()
    {
        super();
    }

    public List<CheckMO> getChecks()
    {
        return checks;
    }

    public void setChecks(List<CheckMO> checks)
    {
        this.checks = checks;
    }

    public List<GroupMO> getParents()
    {
        return parents;
    }

    public void setParents(List<GroupMO> parents)
    {
        this.parents = parents;
    }

    public List<GroupMO> getChildren()
    {
        return children;
    }

    public void setChildren(List<GroupMO> children)
    {
        this.children = children;
    }

    public GroupStateMO getState()
    {
        return state;
    }

    public void setState(GroupStateMO state)
    {
        this.state = state;
    }
}
