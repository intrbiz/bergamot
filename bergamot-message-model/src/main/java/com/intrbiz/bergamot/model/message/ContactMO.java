package com.intrbiz.bergamot.model.message;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Contact message object
 */
@JsonTypeName("bergamot.contact")
public class ContactMO extends SecuredObjectMO
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("email")
    private String email;

    @JsonProperty("pager")
    private String pager;

    @JsonProperty("mobile")
    private String mobile;

    @JsonProperty("phone")
    private String phone;
    
    @JsonProperty("teams")
    private List<TeamMO> teams = new LinkedList<TeamMO>();
    
    @JsonProperty("notifications")
    private NotificationsMO notifications;
    
    @JsonProperty("granted-permissions")
    private List<String> grantedPermissions = new LinkedList<String>();
    
    @JsonProperty("revoked-permissions")
    private List<String> revokedPermissions = new LinkedList<String>();

    public ContactMO()
    {
        super();
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getPager()
    {
        return pager;
    }

    public void setPager(String pager)
    {
        this.pager = pager;
    }

    public String getMobile()
    {
        return mobile;
    }

    public void setMobile(String mobile)
    {
        this.mobile = mobile;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public List<TeamMO> getTeams()
    {
        return teams;
    }

    public void setTeams(List<TeamMO> teams)
    {
        this.teams = teams;
    }

    public NotificationsMO getNotifications()
    {
        return notifications;
    }

    public void setNotifications(NotificationsMO notifications)
    {
        this.notifications = notifications;
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
