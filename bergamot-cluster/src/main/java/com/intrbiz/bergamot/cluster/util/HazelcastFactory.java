package com.intrbiz.bergamot.cluster.util;

import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.Util;

public class HazelcastFactory
{
    private Config hazelcastConfig;

    private HazelcastInstance hazelcastInstance;
    
    public HazelcastFactory()
    {
        super();
    }
    
    public void configure(String instanceName) throws Exception
    {
        // load Hazelcast config
        String hazelcastConfigFile = Util.coalesceEmpty(System.getProperty("hazelcast.config"), System.getenv("hazelcast_config"));
        if (hazelcastConfigFile != null)
        {
            // when using a config file, you must configure the balsa.sessions map
            this.hazelcastConfig = new XmlConfigBuilder(hazelcastConfigFile).build();
        }
        else
        {
            // setup the default configuration
            this.hazelcastConfig = new Config();
        }
        if (instanceName != null) 
            this.hazelcastConfig.setInstanceName(instanceName);
    }
    
    public void start()
    {
        // create the Hazelcast instance
        this.hazelcastInstance = Hazelcast.getOrCreateHazelcastInstance(this.hazelcastConfig);
    }
    
    public Config getHazelcastConfig()
    {
        return this.hazelcastConfig;
    }
    
    public HazelcastInstance getHazelcastInstance()
    {
        return this.hazelcastInstance;
    }
    
    public HazelcastInstance getHazelcastInstance(String instanceName)
    {
        return this.hazelcastInstance;
    }
}
