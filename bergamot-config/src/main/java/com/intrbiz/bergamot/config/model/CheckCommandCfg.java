package com.intrbiz.bergamot.config.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.bergamot.config.resolver.ResolveWith;
import com.intrbiz.bergamot.config.resolver.stratergy.CoalesceEmptyString;
import com.intrbiz.bergamot.config.resolver.stratergy.MergeListUnique;
import com.intrbiz.configuration.CfgParameter;
import com.intrbiz.configuration.Configuration;

@XmlType(name = "check-command")
@XmlRootElement(name = "check-command")
public class CheckCommandCfg extends Configuration
{
    private static final long serialVersionUID = 1L;
    
    private String command;
    
    private String script;

    public CheckCommandCfg()
    {
        super();
    }

    @XmlAttribute(name = "command")
    @ResolveWith(CoalesceEmptyString.class)
    public String getCommand()
    {
        return command;
    }

    public void setCommand(String command)
    {
        this.command = command;
    }
    
    @Override
    @ResolveWith(MergeListUnique.class)
    public List<CfgParameter> getParameters()
    {
        return super.getParameters();
    }
    
    @XmlElement(name = "script")
    @ResolveWith(CoalesceEmptyString.class)
    public String getScript()
    {
        return script;
    }

    public void setScript(String script)
    {
        this.script = script;
    }
}
