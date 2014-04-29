package com.intrbiz.bergamot.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.Util;
import com.intrbiz.bergamot.worker.engine.nagios.NagiosEngine;
import com.intrbiz.bergamot.worker.engine.nagios.NagiosExecutor;
import com.intrbiz.bergamot.worker.engine.nagios.nrpe.NRPEEngine;
import com.intrbiz.bergamot.worker.engine.nagios.nrpe.NRPEExecutor;
import com.intrbiz.configuration.Configuration;

@XmlType(name = "bergamot")
@XmlRootElement(name = "bergamot")
public class BergamotCfg extends Configuration
{
    public enum DaemonMode {
        MASTER, WORKER, BOTH
    };

    private DaemonMode mode = DaemonMode.MASTER;

    private BrokerCfg broker;

    private String configurationDirectory;

    private ManifoldCfg manifold;

    private SchedulerCfg scheduler;

    private ResultProcessorCfg resultProcessor;

    private List<WorkerCfg> workers = new LinkedList<WorkerCfg>();
    
    private List<Macro> macros = new LinkedList<Macro>();
    
    private NotifierCfg notifier;

    public BergamotCfg()
    {
        super();
    }

    @XmlAttribute(name = "mode")
    public DaemonMode getMode()
    {
        return mode;
    }

    public void setMode(DaemonMode mode)
    {
        this.mode = mode;
    }

    @XmlElementRef(type = BrokerCfg.class)
    public BrokerCfg getBroker()
    {
        return broker;
    }

    public void setBroker(BrokerCfg broker)
    {
        this.broker = broker;
    }

    @XmlElement(name = "config-dir")
    public String getConfigurationDirectory()
    {
        return configurationDirectory;
    }

    public void setConfigurationDirectory(String configurationDirectory)
    {
        this.configurationDirectory = configurationDirectory;
    }

    @XmlElementRef(type = ManifoldCfg.class)
    public ManifoldCfg getManifold()
    {
        return manifold;
    }

    public void setManifold(ManifoldCfg manifold)
    {
        this.manifold = manifold;
    }

    @XmlElementRef(type = SchedulerCfg.class)
    public SchedulerCfg getScheduler()
    {
        return scheduler;
    }

    public void setScheduler(SchedulerCfg scheduler)
    {
        this.scheduler = scheduler;
    }

    @XmlElementRef(type = WorkerCfg.class)
    public List<WorkerCfg> getWorkers()
    {
        return workers;
    }

    public void setWorkers(List<WorkerCfg> workers)
    {
        this.workers = workers;
    }

    @XmlElementRef(type = ResultProcessorCfg.class)
    public ResultProcessorCfg getResultProcessor()
    {
        return resultProcessor;
    }

    public void setResultProcessor(ResultProcessorCfg resultProcessor)
    {
        this.resultProcessor = resultProcessor;
    }
    
    
    @XmlElementWrapper(name = "macros")
    @XmlElementRef(type = Macro.class)
    public List<Macro> getMacros()
    {
        return macros;
    }

    public void setMacros(List<Macro> macros)
    {
        this.macros = macros;
    }
    
    @XmlElementRef(type = NotifierCfg.class)
    public NotifierCfg getNotifier()
    {
        return notifier;
    }

    public void setNotifier(NotifierCfg notifier)
    {
        this.notifier = notifier;
    }

    public void applyDefaults()
    {
        // random name
        if (Util.isEmpty(this.getName())) this.setName(UUID.randomUUID().toString());
        // default macros
        if (this.macros.isEmpty())
        {
            this.macros.add(new Macro("USER1", "/usr/lib/nagios/plugins"));
        }
        // default manifold
        if (this.manifold == null) this.manifold = new ManifoldCfg();
        // master / worker defaults
        if (this.getMode() == DaemonMode.MASTER || this.getMode() == DaemonMode.BOTH)
        {
            if (this.scheduler == null) this.scheduler = new SchedulerCfg();
            if (this.resultProcessor == null) this.resultProcessor = new ResultProcessorCfg();
            if (this.notifier == null) this.notifier = new NotifierCfg();
        }
        if (this.getMode() == DaemonMode.WORKER || this.getMode() == DaemonMode.BOTH)
        {
            if (this.workers.isEmpty())
            {
                // default workers
                // nagios engine
                this.workers.add(
                        new WorkerCfg(
                                new ExchangeCfg("bergamot.check.nagios", "topic", true), 
                                new QueueCfg("bergamot.queue.check.nagios", true), 
                                new EngineCfg(
                                        NagiosEngine.class, 
                                        new ExecutorCfg(NagiosExecutor.class
                                )
                        )
                ));
                // nrpe engine
                this.workers.add(
                        new WorkerCfg(
                                new ExchangeCfg("bergamot.check.nrpe", "topic", true), 
                                new QueueCfg("bergamot.queue.check.nrpe", true), 
                                new EngineCfg(
                                        NRPEEngine.class, 
                                        new ExecutorCfg(NRPEExecutor.class
                                )
                        )
                ));
            }
        }
        // cascade defaults down
        this.manifold.applyDefaults();
        if (this.scheduler != null) this.scheduler.applyDefaults();
        if (this.resultProcessor != null) this.resultProcessor.applyDefaults();
        if (this.notifier != null) this.notifier.applyDefaults();
        for (WorkerCfg worker : this.workers)
        {
            worker.applyDefaults();
        }
    }

    public static BergamotCfg read(File file) throws JAXBException, IOException
    {
        return Configuration.read(BergamotCfg.class, new FileInputStream(file));
    }
}
