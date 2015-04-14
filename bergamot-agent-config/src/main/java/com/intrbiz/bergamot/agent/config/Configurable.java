package com.intrbiz.bergamot.agent.config;


public interface Configurable<CFGTYPE extends Configuration>
{
    void configure(CFGTYPE cfg) throws Exception;
    CFGTYPE getConfiguration();
}
