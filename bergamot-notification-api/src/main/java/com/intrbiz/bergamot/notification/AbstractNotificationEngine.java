package com.intrbiz.bergamot.notification;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.accounting.model.AccountingNotificationType;
import com.intrbiz.bergamot.config.NotificationEngineCfg;
import com.intrbiz.bergamot.health.model.KnownDaemon;
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
import com.intrbiz.bergamot.notification.express.StatusColour;
import com.intrbiz.express.DefaultContext;
import com.intrbiz.express.ExpressContext;
import com.intrbiz.express.ExpressExtensionRegistry;
import com.intrbiz.express.template.ExpressTemplate;
import com.intrbiz.express.template.loader.TemplateLoader;
import com.intrbiz.express.template.loader.impl.ClassPathTemplateSource;
import com.intrbiz.express.template.loader.impl.FileTemplateSource;

public abstract class AbstractNotificationEngine implements NotificationEngine
{
    private Logger logger = Logger.getLogger(AbstractNotificationEngine.class);
    
    private final String name;

    protected NotificationEngineCfg config;

    protected Notifier notifier;

    protected final ExpressExtensionRegistry expressExtensions = new ExpressExtensionRegistry("bergamot").addSubRegistry(ExpressExtensionRegistry.getDefaultRegistry());

    protected TemplateLoader templateLoader;

    public AbstractNotificationEngine(String name)
    {
        super();
        this.name = name;
    }

    @Override
    public void configure(NotificationEngineCfg cfg) throws Exception
    {
        this.config = cfg;
        // register our default express functions
        this.expressExtensions.addFunction("status_colour", StatusColour.class);
        // setup our template loader
        this.templateLoader = new TemplateLoader();
        String templatePath = cfg.getStringParameterValue("template.path", "/etc/bergamot/notifier/" + this.getName() + "/templates");
        this.logger.info("Prefering templates from " + templatePath);
        this.templateLoader.addSource(new FileTemplateSource(new File(templatePath)));
        this.templateLoader.addSource(new ClassPathTemplateSource("/templates/" + this.getName()));
        this.templateLoader.setCacheOn(cfg.getBooleanParameterValue("template.cache", true));
        // configure
        this.configure();
    }

    protected void configure() throws Exception
    {
    }

    @Override
    public NotificationEngineCfg getConfiguration()
    {
        return this.config;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public Notifier getNotifier()
    {
        return this.notifier;
    }

    @Override
    public void setNotifier(Notifier notifier)
    {
        this.notifier = notifier;
    }

    public TemplateLoader getTemplateLoader()
    {
        return this.templateLoader;
    }

    public String applyTemplate(String name, Notification notification)
    {
    	ExpressContext context = this.createContext(notification);
        ExpressTemplate template = this.templateLoader.load(context, name);
        if (template != null) return template.encodeToString(context, notification);
        throw new NotificationException("Failed to find template: " + name);
    }
    
    public String applyTemplate(String name, KnownDaemon daemon)
    {
        ExpressContext context = this.createContext(daemon);
        ExpressTemplate template = this.templateLoader.load(context, name);
        if (template != null) return template.encodeToString(context, daemon);
        throw new NotificationException("Failed to find template: " + name);
    }
    
    protected ExpressContext createContext(Notification notification)
    {
        return new DefaultContext(this.expressExtensions, new NotificationEngineEntityResolver(notification), this.templateLoader);
    }
    
    protected ExpressContext createContext(final KnownDaemon daemon)
    {
        ExpressContext ctx = new DefaultContext(this.expressExtensions, new KnownDaemonEntityResolver(daemon), this.templateLoader);
        return ctx;
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
}
