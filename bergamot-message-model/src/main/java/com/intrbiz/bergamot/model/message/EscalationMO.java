package com.intrbiz.bergamot.model.message;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("bergamot.escalation")
public class EscalationMO extends MessageObject
{
    @JsonProperty("after")
    protected long after;
    
    @JsonProperty("time_period")
    protected TimePeriodMO timePeriod;
    
    @JsonProperty("ignore")
    protected Set<String> ignore = new HashSet<String>();
    
    @JsonProperty("contacts")
    protected List<ContactMO> contacts = new LinkedList<ContactMO>();
    
    @JsonProperty("teams")
    protected List<TeamMO> teams = new LinkedList<TeamMO>();
    
    @JsonProperty("renotify")
    protected boolean renotify;
    
    public EscalationMO()
    {
        super();
    }

    public long getAfter()
    {
        return after;
    }

    public void setAfter(long after)
    {
        this.after = after;
    }

    public TimePeriodMO getTimePeriod()
    {
        return timePeriod;
    }

    public void setTimePeriod(TimePeriodMO timePeriod)
    {
        this.timePeriod = timePeriod;
    }

    public Set<String> getIgnore()
    {
        return ignore;
    }

    public void setIgnore(Set<String> ignore)
    {
        this.ignore = ignore;
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

    public boolean isRenotify()
    {
        return renotify;
    }

    public void setRenotify(boolean renotify)
    {
        this.renotify = renotify;
    }
}
