package com.intrbiz.bergamot.model;

import java.util.HashSet;
import java.util.Set;

import com.intrbiz.bergamot.model.message.CheckMO;
import com.intrbiz.bergamot.model.state.CheckState;

/**
 * An something which should be checked
 */
public abstract class Check extends NamedObject
{
    /**
     * How many check attempts to trigger a hard alert
     */
    protected int alertAttemptThreshold = 3;

    /**
     * How many check attempts to trigger a hard recovery
     */
    protected int recoveryAttemptThreshold = 3;

    /**
     * The state of this check
     */
    protected CheckState state = new CheckState();

    /**
     * When should we check, a calendar
     */
    protected TimePeriod checkPeriod;

    /**
     * Is the result of this check suppressed
     */
    protected boolean suppressed = false;

    /**
     * Is this check currently scheduled
     */
    protected boolean enabled = true;

    /**
     * Checks which reference this check
     */
    protected Set<Check> referencedBy = new HashSet<Check>();

    /**
     * Checks which this check references
     */
    protected Set<Check> references = new HashSet<Check>();

    /**
     * The contacts who should be notified
     */
    protected Set<Contact> contacts = new HashSet<Contact>();

    /**
     * When notifications maybe sent for this check
     */
    protected TimePeriod notificationPeriod;

    /**
     * Are notifications enabled for this check
     */
    protected boolean notificationsEnabled;

    public Check()
    {
        super();
    }

    public abstract String getType();

    protected void onSetId()
    {
        this.state.setCheckId(this.id);
    }

    public int getAlertAttemptThreshold()
    {
        return alertAttemptThreshold;
    }

    public void setAlertAttemptThreshold(int alertAttemptThreshold)
    {
        this.alertAttemptThreshold = alertAttemptThreshold;
    }

    public int getRecoveryAttemptThreshold()
    {
        return recoveryAttemptThreshold;
    }

    public void setRecoveryAttemptThreshold(int recoveryAttemptThreshold)
    {
        this.recoveryAttemptThreshold = recoveryAttemptThreshold;
    }

    public int getCurrentAttemptThreshold()
    {
        return (this.getState().isOk() && this.getState().isHard()) ? this.getAlertAttemptThreshold() : this.getRecoveryAttemptThreshold();
    }

    public TimePeriod getCheckPeriod()
    {
        return checkPeriod;
    }

    public void setCheckPeriod(TimePeriod timePeriod)
    {
        this.checkPeriod = timePeriod;
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

    public Set<Check> getReferencedBy()
    {
        return referencedBy;
    }

    public void setReferencedBy(Set<Check> referencedBy)
    {
        this.referencedBy = referencedBy;
    }

    public Set<Check> getReferences()
    {
        return references;
    }

    public void setReferences(Set<Check> references)
    {
        this.references = references;
    }

    public CheckState getState()
    {
        return this.state;
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

    public TimePeriod getNotificationPeriod()
    {
        return notificationPeriod;
    }

    public void setNotificationPeriod(TimePeriod notificationPeriod)
    {
        this.notificationPeriod = notificationPeriod;
    }

    public boolean isNotificationsEnabled()
    {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled)
    {
        this.notificationsEnabled = notificationsEnabled;
    }

    protected void toMO(CheckMO mo)
    {
        super.toMO(mo);
        mo.setAlertAttemptThreshold(this.getAlertAttemptThreshold());
        mo.setEnabled(this.isEnabled());
        mo.setRecoveryAttemptThreshold(this.getRecoveryAttemptThreshold());
        mo.setState(this.getState().toMO());
        mo.setSuppressed(this.isSuppressed());
    }

    /**
     * Get the MessageObject of this check
     */
    public abstract CheckMO toMO();

}
