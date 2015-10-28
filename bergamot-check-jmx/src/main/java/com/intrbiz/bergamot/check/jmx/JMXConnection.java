package com.intrbiz.bergamot.check.jmx;

import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

public class JMXConnection
{    
    private final MBeanServerConnection mBeanServer;
    
    public JMXConnection(MBeanServerConnection mBeanServer)
    {
        super();
        this.mBeanServer = mBeanServer;
    }
    
    public MBeanServerConnection getMBeanServer()
    {
        return this.mBeanServer;
    }

    public List<JMXMBean> getMBeans()
    {
        try
        {
            List<JMXMBean> mbeans = new ArrayList<JMXMBean>();
            for (ObjectInstance oi : this.mBeanServer.queryMBeans(null, null))
            {
                mbeans.add(new JMXMBean(this, oi.getObjectName(), this.mBeanServer.getMBeanInfo(oi.getObjectName())));
            }
            return mbeans;
        }
        catch (Exception e)
        {
            throw new JMXException("Failed to get mbeans", e);
        }
    }
    
    public JMXMBean getMBean(String objectName)
    {
        try
        {
            ObjectName name = ObjectName.getInstance(objectName);
            return new JMXMBean(this, name, this.mBeanServer.getMBeanInfo(name));
        }
        catch (Exception e)
        {
            throw new JMXException("Failed to get mbean", e);
        }
    }
}
