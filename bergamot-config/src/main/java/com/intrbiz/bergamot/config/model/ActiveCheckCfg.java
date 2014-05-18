package com.intrbiz.bergamot.config.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;

import com.intrbiz.bergamot.config.resolver.BeanResolver;
import com.intrbiz.bergamot.config.resolver.ResolveWith;
import com.intrbiz.bergamot.config.resolver.stratergy.Coalesce;

public abstract class ActiveCheckCfg<P extends ActiveCheckCfg<P>> extends RealCheckCfg<P>
{
    private CommandCfg command;

    private ScheduleCfg schedule;

    public ActiveCheckCfg()
    {
        super();
    }

    @XmlElementRef(type = CommandCfg.class)
    @ResolveWith(Coalesce.class)
    public CommandCfg getCommand()
    {
        return command;
    }

    public void setCommand(CommandCfg command)
    {
        this.command = command;
    }

    @XmlElementRef(type = ScheduleCfg.class)
    @ResolveWith(BeanResolver.class)
    public ScheduleCfg getSchedule()
    {
        return schedule;
    }

    public void setSchedule(ScheduleCfg schedule)
    {
        this.schedule = schedule;
    }

    public List<TemplatedObjectCfg<?>> getTemplatedChildObjects()
    {
        List<TemplatedObjectCfg<?>> r = super.getTemplatedChildObjects();
        if (this.command != null)
        {
            r.add(this.command);
        }
        return r;
    }
}
