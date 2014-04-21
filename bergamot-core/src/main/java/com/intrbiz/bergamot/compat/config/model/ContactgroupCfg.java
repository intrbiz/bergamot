package com.intrbiz.bergamot.compat.config.model;

import java.util.List;

import com.intrbiz.bergamot.compat.config.builder.metadata.ParameterName;
import com.intrbiz.bergamot.compat.config.builder.metadata.TypeName;

@TypeName("contactgroup")
public class ContactgroupCfg extends ConfigObject<ContactgroupCfg>
{
    private String contactgroupName;

    private String alias;

    private List<String> members;

    private List<String> contactgroupMembers;

    public ContactgroupCfg()
    {
    }

    public String getContactgroupName()
    {
        return contactgroupName;
    }

    @ParameterName("contactgroup_name")
    public void setContactgroupName(String contactgroupName)
    {
        this.contactgroupName = contactgroupName;
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
    
    public List<String> getContactgroupMembers()
    {
        return contactgroupMembers;
    }

    @ParameterName("contactgroup_members")
    public void setContactgroupMembers(List<String> contactgroupMembers)
    {
        this.contactgroupMembers = contactgroupMembers;
    }
    

    public String resolveContactgroupName()
    {
        return this.resolveProperty((p) -> { return p.getContactgroupName(); });
    }

    public String resolveAlias()
    {
        return this.resolveProperty((p) -> { return p.getAlias(); });
    }

    public List<String> resolveContactgroupMembers()
    {
        return this.resolveProperty((p) -> { return p.getContactgroupMembers(); });
    }

    public List<String> resolveMembers()
    {
        return this.resolveProperty((p) -> { return p.getMembers(); });
    }

    public String toString()
    {
        return "contactgroup { " + this.contactgroupName + " }";
    }
}
