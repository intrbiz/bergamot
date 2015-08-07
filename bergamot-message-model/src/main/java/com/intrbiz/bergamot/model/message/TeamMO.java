package com.intrbiz.bergamot.model.message;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Contact message object
 */
@JsonTypeName("bergamot.team")
public class TeamMO extends SecuredObjectMO
{
    @JsonProperty("teams")
    private List<TeamMO> teams = new LinkedList<TeamMO>();

    @JsonProperty("children")
    private List<TeamMO> children = new LinkedList<TeamMO>();

    @JsonProperty("contacts")
    private List<ContactMO> contacts = new LinkedList<ContactMO>();
    
    @JsonProperty("granted-permissions")
    private List<String> grantedPermissions = new LinkedList<String>();
    
    @JsonProperty("revoked-permissions")
    private List<String> revokedPermissions = new LinkedList<String>();

    public TeamMO()
    {
        super();
    }

    public List<TeamMO> getTeams()
    {
        return teams;
    }

    public void setTeams(List<TeamMO> teams)
    {
        this.teams = teams;
    }

    public List<TeamMO> getChildren()
    {
        return children;
    }

    public void setChildren(List<TeamMO> children)
    {
        this.children = children;
    }

    public List<ContactMO> getContacts()
    {
        return contacts;
    }

    public void setContacts(List<ContactMO> contacts)
    {
        this.contacts = contacts;
    }

    public List<String> getGrantedPermissions()
    {
        return grantedPermissions;
    }

    public void setGrantedPermissions(List<String> grantedPermissions)
    {
        this.grantedPermissions = grantedPermissions;
    }

    public List<String> getRevokedPermissions()
    {
        return revokedPermissions;
    }

    public void setRevokedPermissions(List<String> revokedPermissions)
    {
        this.revokedPermissions = revokedPermissions;
    }
}
