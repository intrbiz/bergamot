package com.intrbiz.bergamot.config.model;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.intrbiz.bergamot.config.adapter.CSVAdapter;
import com.intrbiz.bergamot.config.resolver.BeanResolver;
import com.intrbiz.bergamot.config.resolver.ResolveWith;
import com.intrbiz.bergamot.config.resolver.stratergy.CoalesceEmptyString;
import com.intrbiz.bergamot.config.resolver.stratergy.MergeList;
import com.intrbiz.bergamot.config.resolver.stratergy.SmartMergeSet;

@XmlType(name = "contact")
@XmlRootElement(name = "contact")
public class ContactCfg extends SecuredObjectCfg<ContactCfg> implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Set<String> teams = new LinkedHashSet<String>();

    private String firstName;

    private String preferredName;

    private String familyName;

    private String fullName;

    private String email;

    private String pager;

    private String mobile;

    private String phone;

    private String im;

    private NotificationsCfg notifications;

    private Set<String> grantedPermissions = new LinkedHashSet<String>();

    private Set<String> revokedPermissions = new LinkedHashSet<String>();
    
    private List<AccessControlCfg> accessControls = new LinkedList<AccessControlCfg>();

    public ContactCfg()
    {
        super();
    }

    @XmlJavaTypeAdapter(CSVAdapter.class)
    @XmlAttribute(name = "teams")
    @ResolveWith(SmartMergeSet.class)
    public Set<String> getTeams()
    {
        return teams;
    }

    public void setTeams(Set<String> teams)
    {
        this.teams = teams;
    }

    public void addTeam(String group)
    {
        this.teams.add(group);
    }

    public void removeTeam(String group)
    {
        this.teams.remove(group);
    }

    public boolean containsTeam(String name)
    {
        return this.teams.contains(name);
    }

    @XmlElement(name = "first-name")
    @ResolveWith(CoalesceEmptyString.class)
    public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    @XmlElement(name = "preferred-name")
    @ResolveWith(CoalesceEmptyString.class)
    public String getPreferredName()
    {
        return preferredName;
    }

    public void setPreferredName(String preferredName)
    {
        this.preferredName = preferredName;
    }

    @XmlElement(name = "family-name")
    @ResolveWith(CoalesceEmptyString.class)
    public String getFamilyName()
    {
        return familyName;
    }

    public void setFamilyName(String familyName)
    {
        this.familyName = familyName;
    }

    @XmlElement(name = "full-name")
    @ResolveWith(CoalesceEmptyString.class)
    public String getFullName()
    {
        return fullName;
    }

    public void setFullName(String fullName)
    {
        this.fullName = fullName;
    }

    @XmlElement(name = "email")
    @ResolveWith(CoalesceEmptyString.class)
    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    @XmlElement(name = "pager")
    @ResolveWith(CoalesceEmptyString.class)
    public String getPager()
    {
        return pager;
    }

    public void setPager(String pager)
    {
        this.pager = pager;
    }

    @XmlElement(name = "mobile")
    @ResolveWith(CoalesceEmptyString.class)
    public String getMobile()
    {
        return mobile;
    }

    public void setMobile(String mobile)
    {
        this.mobile = mobile;
    }

    @XmlElement(name = "phone")
    @ResolveWith(CoalesceEmptyString.class)
    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    @XmlElement(name = "im")
    @ResolveWith(CoalesceEmptyString.class)
    public String getIm()
    {
        return im;
    }

    public void setIm(String im)
    {
        this.im = im;
    }

    @XmlElementRef(type = NotificationsCfg.class)
    @ResolveWith(BeanResolver.class)
    public NotificationsCfg getNotifications()
    {
        return notifications;
    }

    public void setNotifications(NotificationsCfg notifications)
    {
        this.notifications = notifications;
    }

    @XmlJavaTypeAdapter(CSVAdapter.class)
    @XmlAttribute(name = "grants")
    @ResolveWith(SmartMergeSet.class)
    public Set<String> getGrantedPermissions()
    {
        return grantedPermissions;
    }

    public void setGrantedPermissions(Set<String> grantedPermissions)
    {
        this.grantedPermissions = grantedPermissions;
    }

    @XmlJavaTypeAdapter(CSVAdapter.class)
    @XmlAttribute(name = "revokes")
    @ResolveWith(SmartMergeSet.class)
    public Set<String> getRevokedPermissions()
    {
        return revokedPermissions;
    }

    public void setRevokedPermissions(Set<String> revokedPermissions)
    {
        this.revokedPermissions = revokedPermissions;
    }

    @XmlElementRef(type = AccessControlCfg.class)
    @ResolveWith(MergeList.class)
    public List<AccessControlCfg> getAccessControls()
    {
        return accessControls;
    }

    public void setAccessControls(List<AccessControlCfg> accessControls)
    {
        this.accessControls = accessControls;
    }

    public List<TemplatedObjectCfg<?>> getTemplatedChildObjects()
    {
        List<TemplatedObjectCfg<?>> r = new LinkedList<TemplatedObjectCfg<?>>();
        return r;
    }
}
