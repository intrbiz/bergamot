package com.intrbiz.bergamot.check.jmx;

import javax.management.MBeanAttributeInfo;

public class MBeanAttributeWrapper
{
    private MBeanWrapper mBean;
    
    private MBeanAttributeInfo attr;
    
    public MBeanAttributeWrapper(MBeanWrapper mBean, MBeanAttributeInfo attr)
    {
        this.mBean = mBean;
        this.attr = attr;
    }
    
    public String getName()
    {
        return this.attr.getName();
    }
    
    public String getType()
    {
        return this.attr.getType();
    }
    
    public String getDescription()
    {
        return this.attr.getDescription();
    }
    
    public Object getValue()
    {
        return this.mBean.getAttributeValue(this.attr.getName());
    }
}
