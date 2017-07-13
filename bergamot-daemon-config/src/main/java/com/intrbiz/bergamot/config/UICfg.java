package com.intrbiz.bergamot.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

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

    private List<BrokerCfg> broker = new LinkedList<BrokerCfg>();

    private DatabaseCfg database;

    private String securityKey;
    
    private UIListenCfg listen;
    
    private LoggingCfg logging;

    public UICfg()
    {
        super();
    }

    @XmlElementRef(type = BrokerCfg.class)
    public List<BrokerCfg> getBroker()
    {
        return broker;
    }

    public void setBroker(List<BrokerCfg> broker)
    {
        this.broker = broker;
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
        // the broker
        if (this.broker == null)
        {
            this.broker = new LinkedList<BrokerCfg>();
        }
        if (this.broker.isEmpty())
        {
            this.broker.add(new BrokerCfg("hcq", new String[] { "ws://127.0.0.1:1543/hcq", "ws://127.0.0.1:1544/hcq", "ws://127.0.0.1:1545/hcq" }));
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
        return new File(Util.coalesceEmpty(System.getProperty("bergamot.config"), System.getenv("bergamot_config"), System.getenv("BERGAMOT_CONFIG"), "/etc/bergamot/ui/default.xml"));
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
