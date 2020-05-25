package com.intrbiz.bergamot;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.UUID;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.intrbiz.Util;

public class BergamotConfig
{
    public static final class Name
    {
        public static final String LOGGING_LEVEL = "logging.level";
        
        public static final String SITE = "site";
        
        public static final String SITES = "sites";
        
        public static final String THREADS = "threads";
        
        public static final String ENABLED_ENGINES = "enabled.engines";
        
        public static final String DISABLED_ENGINES = "disabled.engines";
        
        public static final String WORKER_POOL = "worker.pool";
        
        public static final String PROXY_URL = "proxy.url";
        
        public static final String PROXY_KEY = "proxy.key";
        
        public static final String PROXY_PORT = "proxy.port";
        
        public static final String ZOOKEEPER_NODES = "zookeeper.nodes";
        
        public static final String WEBSOCKET_PORT = "websocket.port";
        
        public static final String DB_URL = "db.url";
        
        public static final String DB_USERNAME = "db.username";
        
        public static final String DB_PASSWORD = "db.password";
        
        public static final String EXPECTED_PROCESSORS = "expected.processors";
        
        public static final String HAZELCAST_PORT = "hazelcast.port";
        
        public static final String HAZELCAST_PUBLIC_ADDRESS = "hazelcast.public.address";
        
        public static final String BERGAMOT_SITE_CONFIG_TEMPLATE = "bergamot.site.config.template";
    }
    
    // Common configuration parameters
    
    public static LinkedHashSet<UUID> getSites()
    {
        LinkedHashSet<UUID> sites = new LinkedHashSet<UUID>();
        sites.addAll(getCSVUUIDConfigurationParameter(Name.SITE));
        sites.addAll(getCSVUUIDConfigurationParameter(Name.SITES));
        return sites;
    }
    
    public static int getThreads(int defaultThreadsPerCore, int defaultOverheadThreads)
    {
        return getIntConfigurationParameter(Name.THREADS, defaultOverheadThreads + (Runtime.getRuntime().availableProcessors() * Math.max(defaultThreadsPerCore, 1)));
    }
    
    public static LinkedHashSet<String> getEnabledEngines()
    {
        return getCSVConfigurationParameter(Name.ENABLED_ENGINES);
    }
    
    public static LinkedHashSet<String> getDisabledEngines()
    {
        return getCSVConfigurationParameter(Name.DISABLED_ENGINES);
    }
    
    public static String getWorkerPool()
    {
        return getConfigurationParameter(Name.WORKER_POOL);
    }
    
    public static String getProxyUrl()
    {
        return getConfigurationParameter(Name.PROXY_URL);
    }
    
    public static String getProxyKey()
    {
        return getConfigurationParameter(Name.PROXY_KEY);
    }
    
    public static int getProxyPort()
    {
        return getIntConfigurationParameter(Name.PROXY_KEY, 14080);
    }
    
    public static String getZooKeeperNodes()
    {
        return getConfigurationParameter(Name.ZOOKEEPER_NODES, "127.0.0.1:2181");
    }
    
    public static int getWebSocketPort()
    {
        return getIntConfigurationParameter(Name.WEBSOCKET_PORT, 8081);
    }
    
    public static String getDbUrl()
    {
        return getConfigurationParameter(Name.DB_URL, "jdbc:postgresql://127.0.0.1:5432/bergamot");
    }
    
    public static String getDbUsername()
    {
        return getConfigurationParameter(Name.DB_USERNAME, "bergamot");
    }
    
    public static String getDbPassword()
    {
        return getConfigurationParameter(Name.DB_PASSWORD, "bergamot");
    }
    
    public static int getExpectedProcessors()
    {
        return getIntConfigurationParameter(Name.EXPECTED_PROCESSORS, 1);
    }
    
    public static int getHazelcastPort()
    {
        return getIntConfigurationParameter(Name.HAZELCAST_PORT, -1);
    }
    
    public static String getHazelcastPublicAddress()
    {
        return getConfigurationParameter(Name.HAZELCAST_PUBLIC_ADDRESS);
    }
    
    public static File getBergamotSiteConfigurationTemplatePath()
    {
        return new File(getConfigurationParameter(Name.BERGAMOT_SITE_CONFIG_TEMPLATE, "/etc/bergamot/config"));
    }
    
    // Config helpers
    
    public static void configureLogging()
    {
        Logger root = Logger.getRootLogger();
        root.addAppender(new ConsoleAppender(new PatternLayout("%d [%t] %p %c %x - %m%n")));
        root.setLevel(Level.toLevel(getConfigurationParameter(Name.LOGGING_LEVEL, "info").toUpperCase()));
    }
    
    // Configuration accessors
    
    public static String toEnvVarName(String name)
    {
        return name.toUpperCase().replace('.', '_').replace('-', '_');
    }
    
    /*
     * Fetch configuration in order of: 
     *   - Env var 
     *   - System property
     *   - Default value
     */
    public static String getConfigurationParameter(String name, String defaultValue)
    {
        return Util.coalesce(Util.coalesceEmpty(System.getenv(toEnvVarName(name)), System.getProperty(name)), defaultValue);
    }
    
    public static String getConfigurationParameter(String name)
    {
        return getConfigurationParameter(name, null);
    }
    
    public static LinkedHashSet<String> getCSVConfigurationParameter(String name)
    {
        LinkedHashSet<String> ret = new LinkedHashSet<String>();
        String values = getConfigurationParameter(name);
        if (values != null)
        {
            for (String value : values.split(","))
            {
                ret.add(value.trim());
            }
        }
        return ret;
    }
    
    public static LinkedHashSet<UUID> getCSVUUIDConfigurationParameter(String name)
    {
        LinkedHashSet<UUID> ret = new LinkedHashSet<UUID>();
        String values = getConfigurationParameter(name);
        if (values != null)
        {
            for (String value : values.split(","))
            {
                ret.add(UUID.fromString(value.trim()));
            }
        }
        return ret;
    }
    
    public static UUID getUUIDConfigurationParameter(String name, UUID defaultValue)
    {
        String value = getConfigurationParameter(name);
        return value == null ? defaultValue : UUID.fromString(value);
    }
    
    public static boolean getBooleanConfigurationParameter(String name, boolean defaultValue)
    {
        String value = getConfigurationParameter(name);
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }

    public static int getIntConfigurationParameter(String name, int defaultValue)
    {
        String value = getConfigurationParameter(name);
        return value == null ? defaultValue : Integer.parseInt(value);
    }
    
    public static long getLongConfigurationParameter(String name, long defaultValue)
    {
        String value = getConfigurationParameter(name);
        return value == null ? defaultValue : Long.parseLong(value);
    }
    
    public static float getFloatConfigurationParameter(String name, float defaultValue)
    {
        String value = getConfigurationParameter(name);
        return value == null ? defaultValue : Float.parseFloat(value);
    }
    
    public static double getDoubleConfigurationParameter(String name, double defaultValue)
    {
        String value = getConfigurationParameter(name);
        return value == null ? defaultValue : Double.parseDouble(value);
    }
}
