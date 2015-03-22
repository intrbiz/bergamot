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

    private BrokerCfg broker;

    private DatabaseCfg database;

    private String securityKey;

    public UICfg()
    {
        super();
    }

    @XmlElementRef(type = BrokerCfg.class)
    public BrokerCfg getBroker()
    {
        return broker;
    }

    public void setBroker(BrokerCfg broker)
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

    @Override
    public void applyDefaults()
    {
        // the broker
        if (this.broker == null)
        {
            this.broker = new BrokerCfg("amqp://127.0.0.1", "bergamot", "bergamot");
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
    }

    public static UICfg read(File file) throws JAXBException, IOException
    {
        return Configuration.read(UICfg.class, new FileInputStream(file));
    }

    /**
     * Load the UI configuration, either from the default config file, the specified config file or the default config.
     */
    public static UICfg loadConfiguration() throws Exception
    {
        UICfg config = null;
        // try the config file?
        File configFile = new File(System.getProperty("bergamot.config", "/etc/bergamot/ui/default.xml"));
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
        File configFile = new File(System.getProperty("bergamot.config", "/etc/bergamot/ui/default.xml"));
        Configuration.write(UICfg.class, this, new FileOutputStream(configFile));
    }
}
