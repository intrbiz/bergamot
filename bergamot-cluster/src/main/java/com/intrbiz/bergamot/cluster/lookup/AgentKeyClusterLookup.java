package com.intrbiz.bergamot.cluster.lookup;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.hazelcast.config.Config;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.config.MapStoreConfig.InitialLoadMode;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MapStoreAdapter;
import com.intrbiz.bergamot.cluster.ObjectNames;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.AgentKey;
import com.intrbiz.data.DataException;

public class AgentKeyClusterLookup extends AgentKeyLookup
{
    private static final int KEY_TTL = (int) TimeUnit.HOURS.toSeconds(24);
    
    public AgentKeyClusterLookup(HazelcastInstance hazelcast)
    {
        super(hazelcast);
    }
    
    protected void configureHazelcast(Config hazelcastConfig)
    {
        // Setup the map TTL, backups
        MapConfig agentMapConfig = hazelcastConfig.getMapConfig(ObjectNames.buildAgentKeyLookupMapName());
        agentMapConfig.setTimeToLiveSeconds(KEY_TTL);
        agentMapConfig.setInMemoryFormat(InMemoryFormat.BINARY);
        agentMapConfig.setBackupCount(0);
        agentMapConfig.setAsyncBackupCount(0);
        // Setup the map loader
        MapStoreConfig storeConfig = agentMapConfig.getMapStoreConfig();
        storeConfig.setImplementation(new AgentKeyLoader());
        storeConfig.setInitialLoadMode(InitialLoadMode.LAZY);
        storeConfig.setEnabled(true);
    }
    
    public static class AgentKeyLoader extends MapStoreAdapter<UUID, AgentKey>
    {
        private static final Logger logger = Logger.getLogger(AgentKeyLoader.class);
        
        @Override
        public AgentKey load(UUID key)
        {
            logger.info("Looking up agent key: " + key);
            try (BergamotDB db = BergamotDB.connect())
            {
                return db.getAgentKey(key);
            }
            catch (DataException e)
            {
                logger.error("Failed to load AgentKey with id " + key);
            }
            return null;
        }
    }
}
