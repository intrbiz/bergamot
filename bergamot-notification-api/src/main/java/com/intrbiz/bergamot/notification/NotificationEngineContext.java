package com.intrbiz.bergamot.notification;

import com.intrbiz.configuration.Configuration;

/**
 * The context this notification engine is executing within
 */
public interface NotificationEngineContext
{
    Configuration getConfiguration();
}
