package com.intrbiz.bergamot.cluster.lookup.hz;

import java.util.UUID;

import org.apache.log4j.Logger;

import com.hazelcast.config.Config;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.config.MapStoreConfig.InitialLoadMode;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.MapStoreAdapter;
import com.intrbiz.bergamot.cluster.util.HZNames;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.AgentKey;
import com.intrbiz.bergamot.model.agent.AgentAuthenticationKey;
import com.intrbiz.data.DataException;

public class HZAgentKeyClusterLookup extends HZAgentKeyLookup
{   
    public HZAgentKeyClusterLookup(HazelcastInstance hazelcast)
    {
        super(hazelcast);
    }
    
    protected void configureHazelcast(Config hazelcastConfig)
    {
        MapConfig agentMapConfig = hazelcastConfig.getMapConfig(HZNames.buildAgentKeyLookupMapName());
        agentMapConfig.setInMemoryFormat(InMemoryFormat.BINARY);
        agentMapConfig.setBackupCount(1);
        agentMapConfig.setAsyncBackupCount(1);
        // Setup the map loader
        MapStoreConfig storeConfig = agentMapConfig.getMapStoreConfig();
        storeConfig.setImplementation(new AgentKeyLoader());
        storeConfig.setInitialLoadMode(InitialLoadMode.LAZY);
        storeConfig.setEnabled(true);
    }
    
    public static class AgentKeyLoader extends MapStoreAdapter<UUID, AgentAuthenticationKey>
    {
        private static final Logger logger = Logger.getLogger(AgentKeyLoader.class);
        
        @Override
        public AgentAuthenticationKey load(UUID id)
        {
            logger.info("Looking up agent key: " + id);
            try (BergamotDB db = BergamotDB.connect())
            {
                AgentKey key = db.getAgentKey(id);
                if (key != null && (! key.isRevoked()))
                {
                    return key.toAgentAuthenticationKey();
                }
            }
            catch (DataException e)
            {
                logger.error("Failed to load AgentKey with id " + id);
            }
            return null;
        }
        
        
    }
}
