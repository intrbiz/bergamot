package com.intrbiz.bergamot.config.model;

import java.util.Date;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.intrbiz.bergamot.config.adapter.DateAdapter;
import com.intrbiz.bergamot.config.resolver.ResolveWith;
import com.intrbiz.bergamot.config.resolver.stratergy.Coalesce;

@XmlType(name = "fixed-period")
@XmlRootElement(name = "fixed-period")
public class SLAFixedPeriodCfg extends SLAPeriodCfg
{
    private static final long serialVersionUID = 1L;
    
    private Date start;
    
    private Date end;

    public SLAFixedPeriodCfg()
    {
        super();
    }

    @XmlAttribute(name = "start")
    @XmlJavaTypeAdapter(DateAdapter.class)
    @ResolveWith(Coalesce.class)
    public Date getStart()
    {
        return start;
    }

    public void setStart(Date start)
    {
        this.start = start;
    }

    @XmlAttribute(name = "end")
    @XmlJavaTypeAdapter(DateAdapter.class)
    @ResolveWith(Coalesce.class)
    public Date getEnd()
    {
        return end;
    }

    public void setEnd(Date end)
    {
        this.end = end;
    }
}
