package com.intrbiz.bergamot.config;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.bergamot.notification.engine.email.EmailEngine;
import com.intrbiz.configuration.CfgParameter;
import com.intrbiz.configuration.Configuration;
import com.intrbiz.queue.name.GenericKey;

@XmlType(name = "notifier")
@XmlRootElement(name = "notifier")
public class NotifierCfg extends Configuration
{
    private int threads = -1;

    private ExchangeCfg exchange;

    private QueueCfg queue;

    private List<String> bindings = new LinkedList<String>();

    private List<NotificationEngineCfg> engines = new LinkedList<NotificationEngineCfg>();

    public NotifierCfg()
    {
        super();
    }

    public NotifierCfg(ExchangeCfg exchange, QueueCfg queue, String binding)
    {
        super();
        this.exchange = exchange;
        this.queue = queue;
        this.bindings.add(binding);
    }

    public NotifierCfg(ExchangeCfg exchange, QueueCfg queue)
    {
        super();
        this.exchange = exchange;
        this.queue = queue;
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

    @XmlElementRef(type = NotificationEngineCfg.class)
    public List<NotificationEngineCfg> getEngines()
    {
        return engines;
    }

    public void setEngines(List<NotificationEngineCfg> engines)
    {
        this.engines = engines;
    }

    @Override
    public void applyDefaults()
    {
        // default number of threads
        if (this.getThreads() <= 0)
        {
            this.setThreads(Runtime.getRuntime().availableProcessors());
        }
        // default exchange and queue
        if (this.exchange == null) this.exchange = new ExchangeCfg("bergamot.notification", "topic", true, "all");
        // default queue
        if (this.queue == null) this.queue = new QueueCfg("bergamot.queue.notification", true);
        // default binding
        if (this.bindings.isEmpty())
        {
            this.bindings.add("#");
        }
        // the engines
        if (this.engines.isEmpty())
        {
            // email engine with default templates
            this.engines.add(new NotificationEngineCfg(
                    EmailEngine.class,
                    new CfgParameter("mail.host", "127.0.0.1"),
                    new CfgParameter("mail.user", ""),
                    new CfgParameter("mail.password", ""),
                    new CfgParameter("from", "bergamot@localhost"),
                    new CfgParameter("service.alert.subject", EmailEngine.Templates.Service.Alert.SUBJECT),
                    new CfgParameter("service.alert.content", EmailEngine.Templates.Service.Alert.CONTENT),
                    new CfgParameter("service.recovery.subject", EmailEngine.Templates.Service.Recovery.SUBJECT),
                    new CfgParameter("service.recovery.content", EmailEngine.Templates.Service.Recovery.CONTENT),
                    new CfgParameter("host.alert.subject", EmailEngine.Templates.Host.Alert.SUBJECT),
                    new CfgParameter("host.alert.content", EmailEngine.Templates.Host.Alert.CONTENT),
                    new CfgParameter("host.recovery.subject", EmailEngine.Templates.Host.Recovery.SUBJECT),
                    new CfgParameter("host.recovery.content", EmailEngine.Templates.Host.Recovery.CONTENT)
            ));
            
        }
    }
}
