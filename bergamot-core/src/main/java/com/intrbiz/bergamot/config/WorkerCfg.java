package com.intrbiz.bergamot.config;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.configuration.Configuration;
import com.intrbiz.queue.name.GenericKey;

@XmlType(name = "worker")
@XmlRootElement(name = "worker")
public class WorkerCfg extends Configuration
{
    private int threads = -1;

    private ExchangeCfg exchange;

    private QueueCfg queue;

    private List<EngineCfg> engines = new LinkedList<EngineCfg>();

    private List<String> bindings = new LinkedList<String>();

    public WorkerCfg()
    {
        super();
    }

    public WorkerCfg(ExchangeCfg exchange, QueueCfg queue, String[] bindings, EngineCfg... engines)
    {
        super();
        for (EngineCfg engine : engines)
        {
            this.engines.add(engine);
        }
        this.exchange = exchange;
        this.queue = queue;
        for (String binding : bindings)
        {
            this.bindings.add(binding);
        }
    }

    public WorkerCfg(ExchangeCfg exchange, QueueCfg queue, String binding, EngineCfg... engines)
    {
        super();
        for (EngineCfg engine : engines)
        {
            this.engines.add(engine);
        }
        this.exchange = exchange;
        this.queue = queue;
        this.bindings.add(binding);
    }

    public WorkerCfg(ExchangeCfg exchange, QueueCfg queue, EngineCfg... engines)
    {
        super();
        for (EngineCfg engine : engines)
        {
            this.engines.add(engine);
        }
        this.exchange = exchange;
        this.queue = queue;
    }

    @XmlElementRef(type=EngineCfg.class)
    public List<EngineCfg> getEngines()
    {
        return engines;
    }

    public void setEngines(List<EngineCfg> engines)
    {
        this.engines = engines;
    }

    @XmlAttribute(name = "threads")
    public int getThreads()
    {
        return threads;
    }

    public void setThreads(int threads)
    {
        this.threads = threads;
    }

    @XmlElementRef(type = ExchangeCfg.class)
    public ExchangeCfg getExchange()
    {
        return exchange;
    }

    public void setExchange(ExchangeCfg exchange)
    {
        this.exchange = exchange;
    }

    @XmlElementRef(type = QueueCfg.class)
    public QueueCfg getQueue()
    {
        return queue;
    }

    public void setQueue(QueueCfg queue)
    {
        this.queue = queue;
    }

    @XmlElement(name = "bind")
    public List<String> getBindings()
    {
        return bindings;
    }

    public void setBindings(List<String> bindings)
    {
        this.bindings = bindings;
    }

    public GenericKey[] asBindings()
    {
        return this.getBindings().stream().map((e) -> {
            return new GenericKey(e);
        }).toArray((size) -> {
            return new GenericKey[size];
        });
    }

    @Override
    public void applyDefaults()
    {
        // default number of threads
        if (this.getThreads() <= 0)
        {
            // default of 5 checks per processor
            this.setThreads(Runtime.getRuntime().availableProcessors() * 5);
        }
        // default binding
        if (this.bindings.isEmpty())
        {
            this.bindings.add("#");
        }
        // cascade
        for (EngineCfg engine : this.engines)
        {
            engine.applyDefaults();
        }
    }
}
