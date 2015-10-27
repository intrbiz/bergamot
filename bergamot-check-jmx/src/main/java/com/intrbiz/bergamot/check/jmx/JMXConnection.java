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

    public List<MBeanWrapper> getMBeans()
    {
        try
        {
            List<MBeanWrapper> mbeans = new ArrayList<MBeanWrapper>();
            for (ObjectInstance oi : this.mBeanServer.queryMBeans(null, null))
            {
                mbeans.add(new MBeanWrapper(this, oi.getObjectName(), this.mBeanServer.getMBeanInfo(oi.getObjectName())));
            }
            return mbeans;
        }
        catch (Exception e)
        {
            throw new JMXException("Failed to get mbeans", e);
        }
    }
    
    public MBeanWrapper getMBean(String objectName)
    {
        try
        {
            ObjectName name = ObjectName.getInstance(objectName);
            return new MBeanWrapper(this, name, this.mBeanServer.getMBeanInfo(name));
        }
        catch (Exception e)
        {
            throw new JMXException("Failed to get mbean", e);
        }
    }
}
