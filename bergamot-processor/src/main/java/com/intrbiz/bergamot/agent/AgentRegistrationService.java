package com.intrbiz.bergamot.agent;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.cluster.broker.AgentEventQueue;
import com.intrbiz.bergamot.cluster.coordinator.NotifierClusterCoordinator;
import com.intrbiz.bergamot.cluster.coordinator.ProcessingPoolClusterCoordinator;
import com.intrbiz.bergamot.config.model.BergamotCfg;
import com.intrbiz.bergamot.config.model.HostCfg;
import com.intrbiz.bergamot.config.validator.ValidatedBergamotConfiguration;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.importer.BergamotConfigImporter;
import com.intrbiz.bergamot.importer.BergamotImportReport;
import com.intrbiz.bergamot.model.Config;
import com.intrbiz.bergamot.model.ConfigChange;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.message.event.agent.AgentEvent;
import com.intrbiz.bergamot.model.message.event.agent.AgentRegister;

public class AgentRegistrationService
{
    private static final Logger logger = Logger.getLogger(AgentRegistrationService.class);
    
    private final AgentEventQueue agentEventQueue;
    
    private final ProcessingPoolClusterCoordinator processingPoolClusterCoordinator;
    
    private final NotifierClusterCoordinator notifierClusterCoordinator;
    
    private volatile boolean run;
    
    private Thread thread;
    
    public AgentRegistrationService(AgentEventQueue agentEventQueue, ProcessingPoolClusterCoordinator processingPoolClusterCoordinator, NotifierClusterCoordinator notifierClusterCoordinator)
    {
        super();
        this.agentEventQueue = agentEventQueue;
        this.processingPoolClusterCoordinator = processingPoolClusterCoordinator;
        this.notifierClusterCoordinator = notifierClusterCoordinator;
    }
    
    public void start()
    {
        this.run = true;
        this.thread = new Thread(this::processAgentEvents, "agent-registration-thread");
        this.thread.start();
    }
    
    public void stop()
    {
        this.run = false;
        try
        {
            this.thread.join();
        }
        catch (InterruptedException e)
        {
        }
    }
    
    protected void processAgentEvents()
    {
        while (this.run)
        {
            try
            {
                AgentEvent event = this.agentEventQueue.poll(500, TimeUnit.MILLISECONDS);
                if (event != null)
                {
                    this.processAgentEvent(event);
                }
            }
            catch (Exception e)
            {
                logger.error("Failed to process agent registration", e);
            }
        }
    }
    
    protected void processAgentEvent(AgentEvent event)
    {
        if (event instanceof AgentRegister)
        {
            this.processAgentRegister((AgentRegister) event);
        }
    }
    
    protected void processAgentRegister(AgentRegister agent)
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            // Is this a new agent which we need to configure
            Host existingAgentHost = db.getHostByAgentId(agent.getSiteId(), agent.getAgentId());
            if (existingAgentHost != null)
            {
                logger.debug("Agent " + agent.getSiteId() + "::" + agent.getAgentId() + " is already registered as host " + existingAgentHost.getId());
                return;
            }
            logger.info("Registering agent for site " + agent.getSiteId() + " from " + agent.getAgentId() + "/" + agent.getAgentName() + " with template " + agent.getTemplateName());
            // Is the site valid
            Site site = db.getSite(agent.getSiteId());
            if (site == null || site.isDisabled())
            {
                logger.warn("Failed to register agent " + agent.getSiteId() + "::" + agent.getAgentId() + " as site is disabled");
                return;
            }
            // Resolve all templates
            List<HostCfg> templateCfgs = new LinkedList<>();
            for (String templateName : agent.getTemplateName().split(","))
            {
                Config templateCfg = db.getConfigByName(agent.getSiteId(), "host", templateName.trim());
                if (templateCfg != null)
                {
                    templateCfgs.add((HostCfg) templateCfg.getConfiguration());
                }
                else
                {
                    // TODO: raise a notification in this instance
                }
            }
            if (templateCfgs.isEmpty())
            {
                logger.warn("Failed to register agent " + agent.getSiteId() + "::" + agent.getAgentId() + " as no such templates " + agent.getTemplateName() + " exists");
                // TODO: raise a notification in this instance
                return;
            }
            // create the configuration change
            BergamotCfg changeCfg = new BergamotCfg();
            changeCfg.setSite(site.getName());
            changeCfg.setSummary("Auto register host: " + agent.getAgentName());
            // the host we are adding
            HostCfg host = new HostCfg();
            host.setAgentId(agent.getAgentId());
            host.setName(agent.getAgentName());
            host.setSummary(Util.coalesceEmpty(agent.getAgentSummary(), agent.getAgentName()));
            host.setAddress(agent.getAgentAddress());
            for (HostCfg templateCfg : templateCfgs)
            {
                host.getInheritedTemplates().add(templateCfg.getName());
            }
            changeCfg.addObject(host);
            // log the config change
            ConfigChange change = new ConfigChange(site.getId(), null, changeCfg);
            db.setConfigChange(change);
            // apply it
            ValidatedBergamotConfiguration validatedConfiguration = changeCfg.validate(db.getObjectLocator(site.getId()));
            if (validatedConfiguration.getReport().isValid())
            {
                // good we have a valid configuration, import it
                BergamotConfigImporter importer = new BergamotConfigImporter(validatedConfiguration).online(this.processingPoolClusterCoordinator.createSchedulerActionProducer(), this.notifierClusterCoordinator, null);
                BergamotImportReport importReport = importer.importConfiguration();
                if (importReport.isSuccessful())
                {
                    // whoop, we successfully registered the host
                    change.setApplied(true);
                    change.setAppliedAt(new Timestamp(System.currentTimeMillis()));
                    db.setConfigChange(change);
                }
                else
                {
                    logger.warn("Failed to auto register agent " + agent.getSiteId() + "::" + agent.getAgentId() + ", the configuration import failed");
                    // keep the config change, it can be manually applied later
                    // TODO: raise a notification in this instance
                }
            }
            else
            {
                logger.warn("Failed to auto register agent " + agent.getSiteId() + "::" + agent.getAgentId() + ", the resultant configuration is not valid");
                // keep the config change, it can be manually applied later
                // TODO: raise a notification in this instance
            }
        }
    }
}
