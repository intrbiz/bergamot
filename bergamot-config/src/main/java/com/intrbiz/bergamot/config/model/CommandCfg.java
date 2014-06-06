package com.intrbiz.bergamot.config.model;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.bergamot.config.resolver.ResolveWith;
import com.intrbiz.bergamot.config.resolver.stratergy.CoalesceEmptyString;

@XmlType(name = "command")
@XmlRootElement(name = "command")
public class CommandCfg extends NamedObjectCfg<CommandCfg>
{
    private String engine;

    private String executor;

    public CommandCfg()
    {
        super();
    }

    @XmlAttribute(name = "engine")
    @ResolveWith(CoalesceEmptyString.class)
    public String getEngine()
    {
        return engine;
    }

    public void setEngine(String engine)
    {
        this.engine = engine;
    }

    @XmlAttribute(name = "executor")
    @ResolveWith(CoalesceEmptyString.class)
    public String getExecutor()
    {
        return executor;
    }

    public void setExecutor(String executor)
    {
        this.executor = executor;
    }

    public List<TemplatedObjectCfg<?>> getTemplatedChildObjects()
    {
        List<TemplatedObjectCfg<?>> r = new LinkedList<TemplatedObjectCfg<?>>();
        return r;
    }
}
