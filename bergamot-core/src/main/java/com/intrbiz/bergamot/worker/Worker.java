package com.intrbiz.bergamot.worker;

import java.util.Collection;

import com.intrbiz.bergamot.component.BergamotComponent;
import com.intrbiz.bergamot.config.WorkerCfg;

/**
 * A worker is responsible for performing tasks.
 * 
 * Workers have a number of engines registered, 
 * engines are specialised, eg: nagios, snmp, etc.
 * 
 */
public interface Worker extends BergamotComponent<WorkerCfg>
{
    Collection<Engine> getEngines();
}
