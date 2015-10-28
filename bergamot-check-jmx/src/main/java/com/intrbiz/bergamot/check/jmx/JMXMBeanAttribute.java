package com.intrbiz.bergamot.check.jmx;

import javax.management.MBeanAttributeInfo;

public class JMXMBeanAttribute
{
    private JMXMBean mBean;
    
    private MBeanAttributeInfo info;
    
    public JMXMBeanAttribute(JMXMBean mBean, MBeanAttributeInfo info)
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
