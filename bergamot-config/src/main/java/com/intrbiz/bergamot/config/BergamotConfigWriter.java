package com.intrbiz.bergamot.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBException;

import com.intrbiz.bergamot.config.model.BergamotCfg;
import com.intrbiz.bergamot.config.model.ClusterCfg;
import com.intrbiz.bergamot.config.model.ContactCfg;
import com.intrbiz.bergamot.config.model.HostCfg;
import com.intrbiz.bergamot.config.model.TeamCfg;
import com.intrbiz.bergamot.config.model.TemplatedObjectCfg;

public class BergamotConfigWriter
{
    private File baseDir;

    private File templates;

    private BergamotCfg config;

    private boolean keepFileStructure = false;

    public BergamotConfigWriter()
    {
        super();
    }

    public BergamotConfigWriter baseDir(File baseDir)
    {
        this.baseDir = baseDir;
        this.templates = new File(this.baseDir, "templates");
        return this;
    }

    public BergamotConfigWriter config(BergamotCfg config)
    {
        this.config = config;
        return this;
    }
    
    public BergamotConfigWriter keepFileStructure(boolean keep)
    {
        this.keepFileStructure = keep;
        return this;
    }

    public void write() throws FileNotFoundException, IOException, JAXBException
    {
        if (this.keepFileStructure)
        {
            this.writeWithStructure();
        }
        else
        {
            this.baseDir.mkdirs();
            // split out the templates
            this.templates.mkdirs();
            this.writeContactTemplates();
            this.writeCommandTemplates();
            this.writeServiceTemplates();
            this.writeTrapTemplates();
            this.writeResourceTemplates();
            this.writeClusterTemplates();
            this.writeHostTemplates();
            // real config
            this.writeGroups();
            this.writeLocations();
            this.writeContacts();
            this.writeTimePeriods();
            this.writeClusters();
            this.writeHosts();
            // site parameters
            this.writeParameters();
        }
    }
    
    private void writeWithStructure() throws FileNotFoundException, IOException, JAXBException
    {
        Map<File, BergamotCfg> configFiles = new HashMap<File, BergamotCfg>();
        // split
        for (List<? extends TemplatedObjectCfg<?>> objects : this.config.getAllObjects())
        {
            for (TemplatedObjectCfg<?> object : objects)
            {
                File file = object.getLoadedFrom();
                if (file != null)
                {
                    BergamotCfg cfg = configFiles.get(file);
                    if (cfg == null)
                    {
                        cfg = new BergamotCfg();
                        cfg.setSite(this.config.getSite());
                        configFiles.put(file, cfg);
                    }
                    cfg.addObject(object);
                }
                else
                {
                    throw new RuntimeException("The config file is not specified for " + object);
                }
            }
        }
        // write
        for (Entry<File, BergamotCfg> configFile : configFiles.entrySet())
        {
            configFile.getKey().getParentFile().mkdirs();
            try (FileOutputStream out = new FileOutputStream(configFile.getKey()))
            {
                BergamotCfg.write(BergamotCfg.class, configFile.getValue(), out);
            }
        }
        // site parameters
        this.writeParameters();
    }
    
    private void writeParameters() throws FileNotFoundException, IOException, JAXBException
    {
        BergamotCfg cfg = new BergamotCfg();
        cfg.setSite(this.config.getSite());
        cfg.getParameters().addAll(this.config.getParameters());
        try (FileOutputStream out = new FileOutputStream(new File(this.baseDir, "parameters.xml")))
        {
            BergamotCfg.write(BergamotCfg.class, cfg, out);
        }
    }

    private void writeContactTemplates() throws FileNotFoundException, IOException, JAXBException
    {
        BergamotCfg cfg = new BergamotCfg();
        cfg.setSite(this.config.getSite());
        for (TeamCfg team : this.config.getTeams())
        {
            if (team.getTemplate() != null && team.getTemplate() == true)
            {
                cfg.getTeams().add(team);
            }
        }
        for (ContactCfg contact : this.config.getContacts())
        {
            if (contact.getTemplate() != null && contact.getTemplate() == true)
            {
                cfg.getContacts().add(contact);
            }
        }
        try (FileOutputStream out = new FileOutputStream(new File(this.templates, "contacts.xml")))
        {
            BergamotCfg.write(BergamotCfg.class, cfg, out);
        }
    }

    private void writeCommandTemplates() throws FileNotFoundException, IOException, JAXBException
    {
        BergamotCfg cfg = new BergamotCfg();
        cfg.setSite(this.config.getSite());
        cfg.getCommands().addAll(this.config.getCommands());
        try (FileOutputStream out = new FileOutputStream(new File(this.templates, "commands.xml")))
        {
            BergamotCfg.write(BergamotCfg.class, cfg, out);
        }
    }

    private void writeServiceTemplates() throws FileNotFoundException, IOException, JAXBException
    {
        BergamotCfg cfg = new BergamotCfg();
        cfg.setSite(this.config.getSite());
        cfg.getServices().addAll(this.config.getServices());
        try (FileOutputStream out = new FileOutputStream(new File(this.templates, "services.xml")))
        {
            BergamotCfg.write(BergamotCfg.class, cfg, out);
        }
    }

    private void writeTrapTemplates() throws FileNotFoundException, IOException, JAXBException
    {
        BergamotCfg cfg = new BergamotCfg();
        cfg.setSite(this.config.getSite());
        cfg.getTraps().addAll(this.config.getTraps());
        try (FileOutputStream out = new FileOutputStream(new File(this.templates, "traps.xml")))
        {
            BergamotCfg.write(BergamotCfg.class, cfg, out);
        }
    }

    private void writeResourceTemplates() throws FileNotFoundException, IOException, JAXBException
    {
        BergamotCfg cfg = new BergamotCfg();
        cfg.setSite(this.config.getSite());
        cfg.getResources().addAll(this.config.getResources());
        try (FileOutputStream out = new FileOutputStream(new File(this.templates, "resources.xml")))
        {
            BergamotCfg.write(BergamotCfg.class, cfg, out);
        }
    }

    private void writeHostTemplates() throws FileNotFoundException, IOException, JAXBException
    {
        BergamotCfg cfg = new BergamotCfg();
        cfg.setSite(this.config.getSite());
        for (HostCfg host : this.config.getHosts())
        {
            if (host.getTemplate() != null && host.getTemplate() == true)
            {
                cfg.getHosts().add(host);
            }
        }
        try (FileOutputStream out = new FileOutputStream(new File(this.templates, "hosts.xml")))
        {
            BergamotCfg.write(BergamotCfg.class, cfg, out);
        }
    }

    private void writeClusterTemplates() throws FileNotFoundException, IOException, JAXBException
    {
        BergamotCfg cfg = new BergamotCfg();
        cfg.setSite(this.config.getSite());
        for (ClusterCfg cluster : this.config.getClusters())
        {
            if (cluster.getTemplate() != null && cluster.getTemplate() == true)
            {
                cfg.getClusters().add(cluster);
            }
        }
        try (FileOutputStream out = new FileOutputStream(new File(this.templates, "cluster.xml")))
        {
            BergamotCfg.write(BergamotCfg.class, cfg, out);
        }
    }

    private void writeGroups() throws FileNotFoundException, IOException, JAXBException
    {
        BergamotCfg cfg = new BergamotCfg();
        cfg.setSite(this.config.getSite());
        cfg.getGroups().addAll(this.config.getGroups());
        try (FileOutputStream out = new FileOutputStream(new File(this.baseDir, "groups.xml")))
        {
            BergamotCfg.write(BergamotCfg.class, cfg, out);
        }
    }

    private void writeLocations() throws FileNotFoundException, IOException, JAXBException
    {
        BergamotCfg cfg = new BergamotCfg();
        cfg.setSite(this.config.getSite());
        cfg.getLocations().addAll(this.config.getLocations());
        try (FileOutputStream out = new FileOutputStream(new File(this.baseDir, "locations.xml")))
        {
            BergamotCfg.write(BergamotCfg.class, cfg, out);
        }
    }

    private void writeTimePeriods() throws FileNotFoundException, IOException, JAXBException
    {
        BergamotCfg cfg = new BergamotCfg();
        cfg.setSite(this.config.getSite());
        cfg.getTimePeriods().addAll(this.config.getTimePeriods());
        try (FileOutputStream out = new FileOutputStream(new File(this.baseDir, "time-periods.xml")))
        {
            BergamotCfg.write(BergamotCfg.class, cfg, out);
        }
    }

    private void writeContacts() throws FileNotFoundException, IOException, JAXBException
    {
        BergamotCfg cfg = new BergamotCfg();
        cfg.setSite(this.config.getSite());
        for (TeamCfg team : this.config.getTeams())
        {
            if (team.getTemplate() == null || team.getTemplate() == false)
            {
                cfg.getTeams().add(team);
            }
        }
        for (ContactCfg contact : this.config.getContacts())
        {
            if (contact.getTemplate() == null || contact.getTemplate() == false)
            {
                cfg.getContacts().add(contact);
            }
        }
        try (FileOutputStream out = new FileOutputStream(new File(this.baseDir, "contacts.xml")))
        {
            BergamotCfg.write(BergamotCfg.class, cfg, out);
        }
    }

    private void writeHosts() throws FileNotFoundException, IOException, JAXBException
    {
        BergamotCfg cfg = new BergamotCfg();
        cfg.setSite(this.config.getSite());
        for (HostCfg host : this.config.getHosts())
        {
            if (host.getTemplate() == null || host.getTemplate() == false)
            {
                cfg.getHosts().add(host);
            }
        }
        try (FileOutputStream out = new FileOutputStream(new File(this.baseDir, "hosts.xml")))
        {
            BergamotCfg.write(BergamotCfg.class, cfg, out);
        }
    }

    private void writeClusters() throws FileNotFoundException, IOException, JAXBException
    {
        BergamotCfg cfg = new BergamotCfg();
        cfg.setSite(this.config.getSite());
        for (ClusterCfg cluster : this.config.getClusters())
        {
            if (cluster.getTemplate() == null || cluster.getTemplate() == false)
            {
                cfg.getClusters().add(cluster);
            }
        }
        try (FileOutputStream out = new FileOutputStream(new File(this.baseDir, "clusters.xml")))
        {
            BergamotCfg.write(BergamotCfg.class, cfg, out);
        }
    }
}
