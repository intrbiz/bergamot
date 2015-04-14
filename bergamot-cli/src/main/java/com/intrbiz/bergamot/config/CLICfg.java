package com.intrbiz.bergamot.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.bergamot.agent.config.Configuration;

@XmlType(name = "bergamot-cli")
@XmlRootElement(name = "bergamot-cli")
public class CLICfg extends Configuration
{
    private static final long serialVersionUID = 1L;

    private List<CLISiteCfg> sites = new LinkedList<CLISiteCfg>();

    public CLICfg()
    {
        super();
    }

    @XmlElementRef(type = CLISiteCfg.class)
    public List<CLISiteCfg> getSites()
    {
        return sites;
    }

    public void setSites(List<CLISiteCfg> sites)
    {
        this.sites = sites;
    }
    
    public CLISiteCfg getSite(String name)
    {
        return this.sites.stream().filter((s) -> { return name.equals(s.getName()); }).findFirst().get();
    }
    
    public void removeSite(String name)
    {
        this.sites.removeIf((s) -> { return name.equals(s.getName()); });
    }
    
    public void addSite(CLISiteCfg site)
    {
        this.sites.add(site);
    }
    
    public void setSite(CLISiteCfg site)
    {
        this.removeSite(site.getName());
        this.addSite(site);
    }

    @Override
    public void applyDefaults()
    {
    }

    public static CLICfg read(File file) throws JAXBException, IOException
    {
        return Configuration.read(CLICfg.class, new FileInputStream(file));
    }

    /**
     * Load the CLI configuration from the users home dir
     */
    public static CLICfg loadConfiguration() throws Exception
    {
        CLICfg config = null;
        // try the config file?
        File configFile = new File(System.getProperty("bergamot.config", System.getProperty("user.home") + "/.bergamot-cli.xml"));
        if (configFile.exists())
        {
            try (FileReader reader = new FileReader(configFile))
            {
                config = Configuration.read(CLICfg.class, reader);
            }
        }
        else
        {
            config = new CLICfg();
        }
        config.applyDefaults();
        return config;
    }
    
    public void saveConfiguration() throws Exception
    {
        File configFile = new File(System.getProperty("bergamot.config", System.getProperty("user.home") + "/.bergamot-cli.xml"));
        try (FileWriter writer = new FileWriter(configFile))
        {
            Configuration.write(CLICfg.class, this, writer);
        }
    }
}
