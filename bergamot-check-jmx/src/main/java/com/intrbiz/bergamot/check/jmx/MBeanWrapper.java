package com.intrbiz.bergamot.check.jmx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

public class MBeanWrapper
{
    private final JMXConnection connection;
    
    private final ObjectName name;
    
    private final MBeanInfo info;
    
    private final Map<String, MBeanAttributeWrapper> attributes = new HashMap<String, MBeanAttributeWrapper>();

    public MBeanWrapper(JMXConnection connection, ObjectName name, MBeanInfo info)
    {
        super();
        this.connection = connection;
        this.name = name;
        this.info = info;
        for (MBeanAttributeInfo attrInfo : info.getAttributes())
        {
            this.attributes.put(attrInfo.getName(), new MBeanAttributeWrapper(this, attrInfo));
        }
    }
    
    public String getDomain()
    {
        return this.name.getDomain();
    }
    
    public String getName()
    {
        return this.name.getCanonicalName();
    }
    
    public String getDescription()
    {
        return this.info.getDescription();
    }
    
    public List<MBeanAttributeWrapper> getAttributes()
    {
        return new ArrayList<MBeanAttributeWrapper>(this.attributes.values());
    }
    
    public MBeanAttributeWrapper getAttribute(String name)
    {
        return this.attributes.get(name);
    }
    
    public Object getAttributeValue(String attribute)
    {
        try
        {
            return this.convertValue(this.connection.getMBeanServer().getAttribute(this.name, attribute));
        }
        catch (Exception e)
        {
            throw new JMXException(e);
        }
    }
    
    private Object convertValue(Object value)
    {
        if (value instanceof CompositeData)
        {
            CompositeData cd = (CompositeData) value;
            Map<String, Object> data = new HashMap<String, Object>();
            for (String name : cd.getCompositeType().keySet())
            {
                data.put(name, this.convertValue(cd.get(name)));
            }
            return data;
        }
        return value;
    }
}
