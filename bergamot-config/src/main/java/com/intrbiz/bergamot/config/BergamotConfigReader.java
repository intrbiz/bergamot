package com.intrbiz.bergamot.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import javax.xml.bind.JAXBException;

import com.intrbiz.bergamot.config.model.BergamotCfg;
import com.intrbiz.bergamot.config.model.TemplatedObjectCfg;
import com.intrbiz.bergamot.config.validator.ValidatedBergamotConfiguration;

public class BergamotConfigReader
{
    private Map<String, BergamotCfg> configurations = new TreeMap<String, BergamotCfg>();
    
    private Stack<File> toParse = new Stack<File>();
    
    public BergamotConfigReader()
    {
        super();
    }
    
    public BergamotConfigReader includeDir(File dir)
    {
        Stack<File> work = new Stack<File>();
        work.push(dir);
        while (!work.isEmpty())
        {
            File f = work.pop();
            if (f.isFile() && f.getName().endsWith(".xml"))
            {
                this.toParse.push(f);
            }
            else if (f.isDirectory())
            {
                for (File c : f.listFiles())
                {
                    work.push(c);
                }
            }
        }
        return this;
    }
    
    public Collection<ValidatedBergamotConfiguration> build()
    {
        this.parse();
        // validate
        List<ValidatedBergamotConfiguration> validated = new LinkedList<ValidatedBergamotConfiguration>();
        for (BergamotCfg cfg : this.configurations.values())
        {
            validated.add(cfg.validate());
        }
        return validated;
    }
    
    private void parse()
    {
        while (! this.toParse.isEmpty())
        {
            File file = this.toParse.pop();
            try (FileInputStream in = new FileInputStream(file))
            {
                BergamotCfg cfg = BergamotCfg.read(BergamotCfg.class, in);
                // set the file the object was loaded from
                for (List<? extends TemplatedObjectCfg<?>> objects : cfg.getAllObjects())
                {
                    for (TemplatedObjectCfg<?> object : objects)
                    {
                        object.setLoadedFrom(file);
                    }
                }
                // merge together
                this.merge(cfg);
            }
            catch (IOException | JAXBException e)
            {
                System.err.println("Failed to parse configuration file: " + file.getAbsolutePath());
                e.printStackTrace();
            }
        }
    }
    
    private void merge(BergamotCfg cfg)
    {
        BergamotCfg existing = this.configurations.get(cfg.getSite());
        if (existing == null)
        {
            this.configurations.put(cfg.getSite(), cfg);
        }
        else
        {
            existing.mergeIn(cfg);
        }
    }
    
    public Collection<BergamotCfg> getConfigurations()
    {
        return this.configurations.values();
    }
}
