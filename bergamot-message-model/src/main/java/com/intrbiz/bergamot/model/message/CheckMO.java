package com.intrbiz.bergamot.model.message;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.intrbiz.bergamot.model.message.state.CheckStateMO;

public abstract class CheckMO extends SecuredObjectMO implements CommentedMO
{
    private static final long serialVersionUID = 1L;

    @JsonProperty("pool")
    protected int pool;
    
    @JsonProperty("state")
    protected CheckStateMO state;

    @JsonProperty("suppressed")
    protected boolean suppressed;

    @JsonProperty("enabled")
    protected boolean enabled;
    
    @JsonProperty("in_downtime")
    protected boolean inDowntime;
    
    @JsonProperty("groups")
    protected List<GroupMO> groups = new LinkedList<GroupMO>();
    
    @JsonProperty("referenced_by")
    protected List<? extends VirtualCheckMO> referencedBy = new LinkedList<VirtualCheckMO>();
    
    @JsonProperty("contacts")
    protected List<ContactMO> contacts = new LinkedList<ContactMO>();
    
    @JsonProperty("teams")
    protected List<TeamMO> teams = new LinkedList<TeamMO>();
    
    @JsonProperty("notifications")
    protected NotificationsMO notifications;
    
    @JsonProperty("external-ref")
    protected String externalRef;
    
    @JsonProperty("note")
    protected NoteMO note;
    
    @JsonProperty("downtime")
    protected List<DowntimeMO> downtime = new LinkedList<DowntimeMO>();
    
    @JsonProperty("comments")
    protected List<CommentMO> comments = new LinkedList<CommentMO>();
    
    @JsonProperty("slas")
    protected List<SLAMO> slas = new LinkedList<SLAMO>();

    public CheckMO()
    {
        super();
    }
    
    @JsonIgnore
    public abstract String getCheckType();

    public int isPool()
    {
        return this.pool;
    }

    public void setPool(int pool)
    {
        this.pool = pool;
    }

    public CheckStateMO getState()
    {
        return state;
    }

    public void setState(CheckStateMO state)
    {
        this.state = state;
    }

    public boolean isSuppressed()
    {
        return suppressed;
    }

    public void setSuppressed(boolean suppressed)
    {
        this.suppressed = suppressed;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public boolean isInDowntime()
    {
        return inDowntime;
    }

    public void setInDowntime(boolean inDowntime)
    {
        this.inDowntime = inDowntime;
    }

    public List<GroupMO> getGroups()
    {
        return groups;
    }

    public void setGroups(List<GroupMO> groups)
    {
        this.groups = groups;
    }

    public List<? extends VirtualCheckMO> getReferencedBy()
    {
        return referencedBy;
    }

    public void setReferencedBy(List<? extends VirtualCheckMO> referencedBy)
    {
        this.referencedBy = referencedBy;
    }

    public List<ContactMO> getContacts()
    {
        return contacts;
    }

    public void setContacts(List<ContactMO> contacts)
    {
        this.contacts = contacts;
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

    public List<DowntimeMO> getDowntime()
    {
        return downtime;
    }

    public void setDowntime(List<DowntimeMO> downtime)
    {
        this.downtime = downtime;
    }

    @Override
    public List<CommentMO> getComments()
    {
        return comments;
    }

    @Override
    public void setComments(List<CommentMO> comments)
    {
        this.comments = comments;
    }

    public List<SLAMO> getSlas()
    {
        return slas;
    }

    public void setSlas(List<SLAMO> slas)
    {
        this.slas = slas;
    }
    
    public String getExternalRef()
    {
        return externalRef;
    }

    public void setExternalRef(String externalRef)
    {
        this.externalRef = externalRef;
    }

    public NoteMO getNote()
    {
        return note;
    }

    public void setNote(NoteMO note)
    {
        this.note = note;
    }
}
