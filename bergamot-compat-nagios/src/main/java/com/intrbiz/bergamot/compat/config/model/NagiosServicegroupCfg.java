package com.intrbiz.bergamot.compat.config.model;

import java.util.List;

import com.intrbiz.bergamot.compat.config.builder.metadata.ParameterName;
import com.intrbiz.bergamot.compat.config.builder.metadata.TypeName;

@TypeName("servicegroup")
public class NagiosServicegroupCfg extends ConfigObject<NagiosServicegroupCfg>
{
    private String servicegroupName;

    private String alias;

    private List<String> members;

    private List<String> servicegroupMembers;

    private String notes;

    private String notesUrl;

    private String actionUrl;

    public NagiosServicegroupCfg()
    {
    }

    public String getServicegroupName()
    {
        return servicegroupName;
    }

    @ParameterName("servicegroup_name")
    public void setServicegroupName(String servicegroupName)
    {
        this.servicegroupName = servicegroupName;
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

    public List<String> getServicegroupMembers()
    {
        return servicegroupMembers;
    }

    @ParameterName("servicegroup_members")
    public void setServicegroupMembers(List<String> servicegroupMembers)
    {
        this.servicegroupMembers = servicegroupMembers;
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
    

    public List<String> resolveMembers()
    {
        return this.resolveProperty((p) -> { return p.getMembers(); });
    }

    public String resolveServicegroupName()
    {
        return this.resolveProperty((p) -> { return p.getServicegroupName(); });
    }

    public String resolveAlias()
    {
        return this.resolveProperty((p) -> { return p.getAlias(); });
    }

    public List<String> resolveServicegroupMembers()
    {
        return this.resolveProperty((p) -> { return p.getServicegroupMembers(); });
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
    
    public String toString()
    {
        return "servicegroup { " + this.servicegroupName + " }";
    }
}
