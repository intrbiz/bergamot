package com.intrbiz.bergamot.notification.template;

import java.io.File;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.message.notification.Notification;
import com.intrbiz.bergamot.notification.AbstractNotificationEngine;
import com.intrbiz.bergamot.notification.NotificationEngineContext;
import com.intrbiz.bergamot.notification.template.express.NotificationEngineEntityResolver;
import com.intrbiz.bergamot.notification.template.express.function.StatusColour;
import com.intrbiz.express.DefaultContext;
import com.intrbiz.express.ExpressContext;
import com.intrbiz.express.ExpressExtensionRegistry;
import com.intrbiz.express.template.ExpressTemplate;
import com.intrbiz.express.template.loader.TemplateLoader;
import com.intrbiz.express.template.loader.impl.ClassPathTemplateSource;
import com.intrbiz.express.template.loader.impl.FileTemplateSource;

public abstract class TemplatedNotificationEngine extends AbstractNotificationEngine
{
    private static final Logger logger = Logger.getLogger(TemplatedNotificationEngine.class);

    protected final ExpressExtensionRegistry expressExtensions = new ExpressExtensionRegistry("bergamot").addSubRegistry(ExpressExtensionRegistry.getDefaultRegistry());

    protected TemplateLoader templateLoader;
    
    public TemplatedNotificationEngine(String vendor, String name, boolean enabledByDefault)
    {
        super(vendor, name, enabledByDefault);
    }

    @Override
    protected void doPrepare(NotificationEngineContext engineContext) throws Exception
    {
        // configure our template engine
        this.configureTemplateEngine(engineContext);
    }
    
    protected void configureTemplateEngine(NotificationEngineContext engineContext)
    {
        // register our default express functions
        this.expressExtensions.addFunction("status_colour", StatusColour.class);
        // setup our template loader
        this.templateLoader = new TemplateLoader();
        String templatePath = engineContext.getParameter("template." + this.getName() + ".path", "/etc/bergamot/notifier/" + this.getName() + "/templates");
        logger.info("Prefering templates from " + templatePath);
        this.templateLoader.addSource(new FileTemplateSource(new File(templatePath)));
        this.templateLoader.addSource(new ClassPathTemplateSource("/templates/" + this.getName()));
        this.templateLoader.setCacheOn(engineContext.getBooleanParameter("template.cache", true));
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
    
    protected ExpressContext createContext(Notification notification)
    {
        return new DefaultContext(this.expressExtensions, new NotificationEngineEntityResolver(notification), this.templateLoader);
    }
}
