package com.intrbiz.bergamot.check.jmx;

import javax.management.MBeanParameterInfo;

public class JMXMBeanOperationParameter
{    
    private final MBeanParameterInfo param;
    
    public JMXMBeanOperationParameter(MBeanParameterInfo param)
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
