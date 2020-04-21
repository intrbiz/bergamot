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
import com.intrbiz.bergamot.model.ProxyKey;
import com.intrbiz.data.DataException;

public class HZProxyKeyClusterLookup extends HZProxyKeyLookup
{   
    public HZProxyKeyClusterLookup(HazelcastInstance hazelcast)
    {
        super(hazelcast);
    }
    
    protected void configureHazelcast(Config hazelcastConfig)
    {
        MapConfig agentMapConfig = hazelcastConfig.getMapConfig(HZNames.buildProxyKeyLookupMapName());
        agentMapConfig.setInMemoryFormat(InMemoryFormat.BINARY);
        agentMapConfig.setBackupCount(1);
        agentMapConfig.setAsyncBackupCount(1);
        // Setup the map loader
        MapStoreConfig storeConfig = agentMapConfig.getMapStoreConfig();
        storeConfig.setImplementation(new ProxyKeyLoader());
        storeConfig.setInitialLoadMode(InitialLoadMode.LAZY);
        storeConfig.setEnabled(true);
    }
    
    public static class ProxyKeyLoader extends MapStoreAdapter<UUID, ProxyKey>
    {
        private static final Logger logger = Logger.getLogger(ProxyKeyLoader.class);
        
        @Override
        public ProxyKey load(UUID key)
        {
            logger.info("Looking up proxy key: " + key);
            try (BergamotDB db = BergamotDB.connect())
            {
                return db.getProxyKey(key);
            }
            catch (DataException e)
            {
                logger.error("Failed to load ProxyKey with id " + key);
            }
            return null;
        }
        
        
    }
}
