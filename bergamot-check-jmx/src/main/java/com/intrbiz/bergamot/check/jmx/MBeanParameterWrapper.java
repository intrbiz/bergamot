package com.intrbiz.bergamot.check.jmx;

import javax.management.MBeanParameterInfo;

public class MBeanParameterWrapper
{    
    private final MBeanParameterInfo param;
    
    public MBeanParameterWrapper(MBeanParameterInfo param)
    {
        this.param = param;
    }
    
    public String getType()
    {
        return param.getType();
    }
    
    public String getName()
    {
        return param.getName();
    }
    
    public String getDescription()
    {
        return this.param.getDescription();
    }
}
