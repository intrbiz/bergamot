package com.intrbiz.bergamot.check.jmx;

import javax.management.MBeanAttributeInfo;

public class MBeanAttributeWrapper
{
    private MBeanWrapper mBean;
    
    private MBeanAttributeInfo info;
    
    public MBeanAttributeWrapper(MBeanWrapper mBean, MBeanAttributeInfo info)
    {
        this.mBean = mBean;
        this.info = info;
    }
    
    public String getName()
    {
        return this.info.getName();
    }
    
    public String getType()
    {
        return this.info.getType();
    }
    
    public String getDescription()
    {
        return this.info.getDescription();
    }
    
    public <T> T getValue()
    {
        return this.mBean.getAttributeValue(this.info.getName());
    }
}
