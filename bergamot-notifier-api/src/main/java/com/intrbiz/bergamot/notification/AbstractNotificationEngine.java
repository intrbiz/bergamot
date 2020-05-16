package com.intrbiz.bergamot.notification;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import com.intrbiz.Util;
import com.intrbiz.bergamot.accounting.model.AccountingNotificationType;
import com.intrbiz.bergamot.model.message.CheckMO;
import com.intrbiz.bergamot.model.message.ContactMO;
import com.intrbiz.bergamot.model.message.ParameterMO;
import com.intrbiz.bergamot.model.message.ParameterisedMO;
import com.intrbiz.bergamot.model.message.ResourceMO;
import com.intrbiz.bergamot.model.message.ServiceMO;
import com.intrbiz.bergamot.model.message.TeamMO;
import com.intrbiz.bergamot.model.message.TrapMO;
import com.intrbiz.bergamot.model.message.notification.CheckNotification;
import com.intrbiz.bergamot.model.message.notification.Notification;
import com.intrbiz.bergamot.model.message.notification.PasswordResetNotification;
import com.intrbiz.bergamot.model.message.notification.RegisterContactNotification;
import com.intrbiz.bergamot.model.message.notification.SendAcknowledge;
import com.intrbiz.bergamot.model.message.notification.SendAlert;
import com.intrbiz.bergamot.model.message.notification.SendRecovery;
import com.intrbiz.bergamot.model.message.notification.SendUpdate;

public abstract class AbstractNotificationEngine implements NotificationEngine
{   
    private final String vendor;
    
    private final String name;
    
    private final boolean enabledByDefault;

    protected NotificationEngineContext engineContext;

    public AbstractNotificationEngine(final String vendor, final String name, final boolean enabledByDefault)
    {
        super();
        this.vendor = Objects.requireNonNull(vendor);
        this.name = Objects.requireNonNull(name);
        this.enabledByDefault = enabledByDefault;
    }

    @Override
    public String getName()
    {
        return this.name;
    }
    
    @Override
    public String getVendor()
    {
        return this.vendor;
    }

    @Override
    public boolean isEnabledByDefault()
    {
        return this.enabledByDefault;
    }

    protected final NotificationEngineContext getEngineContext()
    {
        return this.engineContext;
    }
    
    @Override
    public final void prepare(NotificationEngineContext context) throws Exception
    {
        this.engineContext = context;
        this.doPrepare(context);
    }
    
    protected void doPrepare(NotificationEngineContext engineContext) throws Exception
    {
    }
    
    @Override
    public final void start(NotificationEngineContext context) throws Exception
    {
        this.doStart(context);
    }
    
    protected void doStart(NotificationEngineContext engineContext) throws Exception
    {
    }

    public final void shutdown(NotificationEngineContext engineContext)
    {
        this.doShutdown(engineContext);
    }
    
    protected void doShutdown(NotificationEngineContext engineContext)
    {
    }
    
    
    // util helpers
    
    /**
     * Look through a check notification for any parameters where the name start with the given prefix collecting the set of values
     * @param notification the notification
     * @param parameterNamePrefix find parameters where the name starts this prefix
     * @return a unique set of parameter values
     */
    protected Set<String> findNotificationParameters(CheckNotification notification, String parameterNamePrefix)
    {
        Set<String> urls = new TreeSet<String>();
        // search contacts and teams
        for (ContactMO to : notification.getTo())
        {
            this.findNotificationParameters(to, parameterNamePrefix, urls);
            for (TeamMO team : to.getTeams())
            {
                this.findNotificationParameters(team, parameterNamePrefix, urls);
            }
        }
        // search the check objects
        CheckMO check = notification.getCheck();
        this.findNotificationParameters(check, parameterNamePrefix, urls);
        // go up the chain
        if (check instanceof ServiceMO)  this.findNotificationParameters(((ServiceMO) check).getHost(),     parameterNamePrefix, urls);
        if (check instanceof TrapMO)     this.findNotificationParameters(((TrapMO) check).getHost(),        parameterNamePrefix, urls);
        if (check instanceof ResourceMO) this.findNotificationParameters(((ResourceMO) check).getCluster(), parameterNamePrefix, urls);
        // done
        return urls;
    }
    
    protected void findNotificationParameters(ParameterisedMO params, String parameterNamePrefix, Set<String> urls)
    {
        for (ParameterMO urlParam : params.getParametersStartingWith("slack.url"))
        {
            if (! Util.isEmpty(urlParam.getValue()))
                urls.add(urlParam.getValue());
        }
    }
    
    // accounting helpers
    
    public static AccountingNotificationType getNotificationType(Notification notification)
    {
        if (notification instanceof SendAlert) return AccountingNotificationType.ALERT;
        else if (notification instanceof SendRecovery) return AccountingNotificationType.RECOVERY;
        else if (notification instanceof SendAcknowledge) return AccountingNotificationType.ACKNOWLEDGEMENT;
        else if (notification instanceof SendUpdate) return AccountingNotificationType.UPDATE;
        else if (notification instanceof PasswordResetNotification) return AccountingNotificationType.RESET;
        else if (notification instanceof RegisterContactNotification) return AccountingNotificationType.REGISTER;
        return null;
    }
    
    public static UUID getObjectId(Notification notification)
    {
        if (notification instanceof CheckNotification) return ((CheckNotification) notification).getCheck().getId();
        else if (notification instanceof PasswordResetNotification) return ((PasswordResetNotification) notification).getContact().getId();
        else if (notification instanceof RegisterContactNotification) return ((RegisterContactNotification) notification).getContact().getId();
        return null;
    }
    
    public String toString()
    {
        return this.vendor + "::" + this.name;
    }
}
