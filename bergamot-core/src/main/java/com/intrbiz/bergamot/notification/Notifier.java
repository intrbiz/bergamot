package com.intrbiz.bergamot.notification;

import java.util.List;

import com.intrbiz.bergamot.component.BergamotComponent;
import com.intrbiz.bergamot.config.NotifierCfg;

public interface Notifier extends BergamotComponent<NotifierCfg>
{
    List<NotificationEngine> getEngines();
}
