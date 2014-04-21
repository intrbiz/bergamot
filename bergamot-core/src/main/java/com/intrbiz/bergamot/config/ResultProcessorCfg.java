package com.intrbiz.bergamot.config;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.configuration.Configuration;
import com.intrbiz.queue.name.GenericKey;

@XmlType(name = "result-processor")
@XmlRootElement(name = "result-processor")
public class ResultProcessorCfg extends Configuration
{
    private ExchangeCfg exchange;

    private QueueCfg queue;

    private List<String> bindings = new LinkedList<String>();
    
    public ResultProcessorCfg()
    {
        super();
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
        return this.getBindings().stream().map((e) -> { return new GenericKey(e); }).toArray((size) -> { return new GenericKey[size]; });
    }

    @Override
    public void applyDefaults()
    {
        if (this.exchange == null) this.exchange = new ExchangeCfg("bergamot.result", "topic", true);
        if (this.queue == null) this.queue = new QueueCfg("bergamot.queue.result", true);
        if (this.bindings.isEmpty()) this.bindings.add("#");
    }
}
