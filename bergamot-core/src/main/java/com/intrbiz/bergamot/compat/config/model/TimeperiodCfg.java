package com.intrbiz.bergamot.compat.config.model;

import java.util.LinkedList;
import java.util.List;

import com.intrbiz.bergamot.compat.config.builder.metadata.ParameterName;
import com.intrbiz.bergamot.compat.config.builder.metadata.TypeName;
import com.intrbiz.bergamot.compat.config.parser.model.ObjectParameter;

@TypeName("timeperiod")
public class TimeperiodCfg extends ConfigObject<TimeperiodCfg>
{
    private String timeperiodName;

    private String alias;

    private List<String> exclude;

    private List<String> periods;

    public TimeperiodCfg()
    {
        super();
    }

    public String getTimeperiodName()
    {
        return timeperiodName;
    }

    @ParameterName("timeperiod_name")
    public void setTimeperiodName(String timeperiodName)
    {
        this.timeperiodName = timeperiodName;
    }

    public String getAlias()
    {
        return alias;
    }

    @ParameterName("alias")
    public void setAlias(String alias)
    {
        this.alias = alias;
    }

    public List<String> getExclude()
    {
        return exclude;
    }

    @ParameterName("exclude")
    public void setExclude(List<String> exclude)
    {
        this.exclude = exclude;
    }

    public List<String> getPeriods()
    {
        return periods;
    }

    public void setPeriods(List<String> periods)
    {
        this.periods = periods;
    }

    @Override
    public boolean unhandledObjectParameter(ObjectParameter parameter)
    {
        if (this.periods == null)
        {
            this.periods = new LinkedList<String>();
        }
        this.periods.add(parameter.getName() + " " + parameter.getValue());
        return true;
    }
    

    public String resolveTimeperiodName()
    {
        return this.resolveProperty((p) -> { return p.getTimeperiodName(); });
    }

    public String resolveAlias()
    {
        return this.resolveProperty((p) -> { return p.getAlias(); });
    }

    public List<String> resolveExclude()
    {
        return this.resolveProperty((p) -> { return p.getExclude(); });
    }

    public List<String> resolvePeriods()
    {
        return this.resolveProperty((p) -> { return p.getPeriods(); });
    }



    public String toString()
    {
        return "timeperiod { " + this.timeperiodName + " }";
    }

}
