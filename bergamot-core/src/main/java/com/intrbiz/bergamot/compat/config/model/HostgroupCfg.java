package com.intrbiz.bergamot.compat.config.model;

import java.util.List;

import com.intrbiz.bergamot.compat.config.builder.metadata.ParameterName;
import com.intrbiz.bergamot.compat.config.builder.metadata.TypeName;

@TypeName("hostgroup")
public class HostgroupCfg extends ConfigObject<HostgroupCfg>
{
    private String hostgroupName;

    private String alias;

    private List<String> members;

    private List<String> hostgroupMembers;

    private String notes;

    private String notesUrl;

    private String actionUrl;

    public HostgroupCfg()
    {
    }

    public String getHostgroupName()
    {
        return hostgroupName;
    }

    @ParameterName("hostgroup_name")
    public void setHostgroupName(String hostgroupName)
    {
        this.hostgroupName = hostgroupName;
    }

    public String getAlias()
    {
        return alias;
    }

    @ParameterName("alias")
    public void setAlias(String alias)
    {
        this.alias = alias;
    }

    public List<String> getMembers()
    {
        return members;
    }

    @ParameterName("members")
    public void setMembers(List<String> members)
    {
        this.members = members;
    }

    public List<String> getHostgroupMembers()
    {
        return hostgroupMembers;
    }

    @ParameterName("hostgroup_members")
    public void setHostgroupMembers(List<String> hostgroupMembers)
    {
        this.hostgroupMembers = hostgroupMembers;
    }

    public String getNotes()
    {
        return notes;
    }

    @ParameterName("notes")
    public void setNotes(String notes)
    {
        this.notes = notes;
    }

    public String getNotesUrl()
    {
        return notesUrl;
    }

    @ParameterName("notes_url")
    public void setNotesUrl(String notesUrl)
    {
        this.notesUrl = notesUrl;
    }

    public String getActionUrl()
    {
        return actionUrl;
    }

    @ParameterName("action_url")
    public void setActionUrl(String actionUrl)
    {
        this.actionUrl = actionUrl;
    }
    

    public String resolveHostgroupName()
    {
        return this.resolveProperty((p) -> { return p.getHostgroupName(); });
    }

    public String resolveAlias()
    {
        return this.resolveProperty((p) -> { return p.getAlias(); });
    }

    public List<String> resolveHostgroupMembers()
    {
        return this.resolveProperty((p) -> { return p.getHostgroupMembers(); });
    }

    public String resolveNotes()
    {
        return this.resolveProperty((p) -> { return p.getNotes(); });
    }

    public String resolveNotesUrl()
    {
        return this.resolveProperty((p) -> { return p.getNotesUrl(); });
    }

    public String resolveActionUrl()
    {
        return this.resolveProperty((p) -> { return p.getActionUrl(); });
    }

    public List<String> resolveMembers()
    {
        return this.resolveProperty((p) -> { return p.getMembers(); });
    }
    
    public String toString()
    {
        return "hostgroup { " + this.hostgroupName + " }";
    }
}
