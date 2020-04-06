package com.intrbiz.bergamot.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.Util;
import com.intrbiz.configuration.Configuration;
import com.intrbiz.crypto.SecretKey;

@XmlType(name = "ui")
@XmlRootElement(name = "ui")
public class UICfg extends Configuration
{
    private static final long serialVersionUID = 1L;

    private ClusterCfg cluster;
    
    private DatabaseCfg database;

    private String securityKey;
    
    private UIListenCfg listen;
    
    private LoggingCfg logging;

    public UICfg()
    {
        super();
    }

    @XmlElementRef(type = ClusterCfg.class)
    public ClusterCfg getCluster()
    {
        return this.cluster;
    }

    public void setCluster(ClusterCfg cluster)
    {
        this.cluster = cluster;
    }

    @XmlElementRef(type = DatabaseCfg.class)
    public DatabaseCfg getDatabase()
    {
        return database;
    }

    public void setDatabase(DatabaseCfg database)
    {
        this.database = database;
    }

    @XmlElement(name = "security-key")
    public String getSecurityKey()
    {
        return securityKey;
    }

    public void setSecurityKey(String securityKey)
    {
        this.securityKey = securityKey;
    }

    @XmlElementRef(type = UIListenCfg.class)
    public UIListenCfg getListen()
    {
        return listen;
    }

    public void setListen(UIListenCfg listen)
    {
        this.listen = listen;
    }

    @XmlElementRef(type = LoggingCfg.class)
    public LoggingCfg getLogging()
    {
        return logging;
    }

    public void setLogging(LoggingCfg logging)
    {
        this.logging = logging;
    }

    @Override
    public void applyDefaults()
    {
        // the network
        if (this.cluster == null)
        {
            this.cluster = new ClusterCfg(new String[] { "127.0.0.1:2181" });
        }
        // the database
        if (this.database == null)
        {
            this.database = new DatabaseCfg("jdbc:postgresql://127.0.0.1:5432/bergamot", "bergamot", "bergamot");
        }
        // the security key
        if (Util.isEmpty(securityKey))
        {
            this.securityKey = SecretKey.generate().toString();
        }
        // listen defaults
        if (this.listen == null)
        {
            this.listen = new UIListenCfg();
        }
        // logging defaults
        if (this.logging == null)
        {
            this.logging = new LoggingCfg();
        }
    }

    public static UICfg read(File file) throws JAXBException, IOException
    {
        return Configuration.read(UICfg.class, new FileInputStream(file));
    }
    
    /**
     * Search for the configuration file
     */
    public static File getConfigurationFile()
    {
        return new File(Util.coalesceEmpty(System.getProperty("bergamot.config"), System.getenv("BERGAMOT_CONFIG"), "/etc/bergamot/ui/default.xml"));
    }

    /**
     * Load the UI configuration, either from the default config file, the specified config file or the default config.
     */
    public static UICfg loadConfiguration() throws Exception
    {
        UICfg config = null;
        // try the config file?
        File configFile = getConfigurationFile();
        if (configFile.exists())
        {
            config = Configuration.read(UICfg.class, new FileInputStream(configFile));
        }
        else
        {
            config = new UICfg();
        }
        config.applyDefaults();
        return config;
    }
    
    /**
     * Save the UI configuration, either to the default config file, the specified config file.
     */
    public void saveConfiguration() throws Exception
    {
        File configFile = getConfigurationFile();
        Configuration.write(UICfg.class, this, new FileOutputStream(configFile));
    }
}
