package com.intrbiz.bergamot.notification;

import com.intrbiz.Util;

/**
 * The context this notification engine is executing within
 */
public interface NotificationEngineContext
{
    /**
     * Get the notifier configuration
     */
    default String getParameter(String name, String defaultValue)
    {
        return defaultValue;
    }
    
    default int getIntParameter(String name, int defaultValue)
    {
        String value = this.getParameter(name, null);
        return ! Util.isEmpty(value) ? Integer.parseInt(value) : defaultValue;
    }
    
    default long getLongParameter(String name, long defaultValue)
    {
        String value = this.getParameter(name, null);
        return ! Util.isEmpty(value) ? Long.parseLong(value) : defaultValue;
    }
    
    default float getFloatParameter(String name, float defaultValue)
    {
        String value = this.getParameter(name, null);
        return ! Util.isEmpty(value) ? Float.parseFloat(value) : defaultValue;
    }
    
    default double getDoubleParameter(String name, double defaultValue)
    {
        String value = this.getParameter(name, null);
        return ! Util.isEmpty(value) ? Double.parseDouble(value) : defaultValue;
    }
    
    default boolean getBooleanParameter(String name, boolean defaultValue)
    {
        String value = this.getParameter(name, null);
        return ! Util.isEmpty(value) ? Boolean.parseBoolean(value) : defaultValue;
    }
}
