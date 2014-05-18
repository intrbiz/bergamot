package com.intrbiz.bergamot.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.intrbiz.bergamot.model.message.CheckMO;
import com.intrbiz.bergamot.model.state.CheckState;

/**
 * An something which should be checked
 */
public abstract class Check extends NamedObject
{
    /**
     * The state of this check
     */
    protected CheckState state = new CheckState();

    /**
     * Is the result of this check suppressed
     */
    protected boolean suppressed = false;

    /**
     * Is this check currently scheduled
     */
    protected boolean enabled = true;

    /**
     * Checks which reference (are dependent upon) this check
     */
    protected Set<VirtualCheck> referencedBy = new HashSet<VirtualCheck>();

    /**
     * The contacts who should be notified
     */
    protected Set<Contact> contacts = new HashSet<Contact>();

    /**
     * The reams who should be notified
     */
    protected Set<Team> teams = new HashSet<Team>();

    /**
     * When and how notifications may be send
     */
    protected Notifications notifications;

    /**
     * The groups this check is a member of
     */
    protected Map<String, Group> groups = new HashMap<String, Group>();

    public Check()
    {
        super();
    }

    public abstract String getType();

    protected void onSetId()
    {
        this.state.setCheckId(this.id);
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

    public Set<VirtualCheck> getReferencedBy()
    {
        return referencedBy;
    }

    public void setReferencedBy(Set<VirtualCheck> referencedBy)
    {
        this.referencedBy = referencedBy;
    }

    public void addReferencedBy(VirtualCheck referencedBy)
    {
        this.referencedBy.add(referencedBy);
    }

    public CheckState getState()
    {
        return this.state;
    }

    public Set<Contact> getAllContacts()
    {
        Set<Contact> ret = new HashSet<Contact>();
        ret.addAll(this.getContacts());
        for (Team team : this.getTeams())
        {
            ret.addAll(team.getAllContacts());
        }
        return ret;
    }

    public Set<Contact> getContacts()
    {
        return contacts;
    }

    public void setContacts(Set<Contact> contacts)
    {
        this.contacts = contacts;
    }

    public void addContact(Contact contact)
    {
        this.contacts.add(contact);
    }

    public Set<Team> getTeams()
    {
        return teams;
    }

    public void setTeams(Set<Team> teams)
    {
        this.teams = teams;
    }

    public void addTeam(Team team)
    {
        this.teams.add(team);
    }

    public Notifications getNotifications()
    {
        return notifications;
    }

    public void setNotifications(Notifications notifications)
    {
        this.notifications = notifications;
    }

    public Collection<Group> getGroups()
    {
        return groups.values();
    }

    public void removeGroup(Group group)
    {
        this.groups.remove(group.getName());
    }

    public void addGroup(Group group)
    {
        this.groups.put(group.getName(), group);
    }

    protected void toMO(CheckMO mo)
    {
        super.toMO(mo);
        mo.setEnabled(this.isEnabled());
        mo.setState(this.getState().toMO());
        mo.setSuppressed(this.isSuppressed());
    }

    /**
     * Get the MessageObject of this check
     */
    public abstract CheckMO toMO();

}
