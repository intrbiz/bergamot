package com.intrbiz.bergamot.compat.config.builder;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.compat.config.builder.object.ObjectBuilder;
import com.intrbiz.bergamot.compat.config.model.CommandCfg;
import com.intrbiz.bergamot.compat.config.model.ConfigObject;
import com.intrbiz.bergamot.compat.config.model.ContactCfg;
import com.intrbiz.bergamot.compat.config.model.ContactgroupCfg;
import com.intrbiz.bergamot.compat.config.model.HostCfg;
import com.intrbiz.bergamot.compat.config.model.HostgroupCfg;
import com.intrbiz.bergamot.compat.config.model.ServiceCfg;
import com.intrbiz.bergamot.compat.config.model.ServicegroupCfg;
import com.intrbiz.bergamot.compat.config.model.TimeperiodCfg;
import com.intrbiz.bergamot.compat.config.parser.NagiosConfigParser;
import com.intrbiz.bergamot.compat.config.parser.model.Directive;
import com.intrbiz.bergamot.compat.config.parser.model.IncludeDir;
import com.intrbiz.bergamot.compat.config.parser.model.IncludeFile;
import com.intrbiz.bergamot.compat.config.parser.model.ObjectDefinition;

/**
 * Read Nagios config files / directories and construct a object 
 * model of the configuration.  Resolving inheritance but not 
 * constructing a complete object model. 
 * 
 */
public class NagiosConfigBuilder
{
    private static final Map<String, ObjectBuilder> builders = new TreeMap<String, ObjectBuilder>();

    private static final void registerObjectBuilder(ObjectBuilder builder)
    {
        builders.put(builder.getType(), builder);
    }

    static
    {
        registerObjectBuilder(ObjectBuilder.create(HostCfg.class));
        registerObjectBuilder(ObjectBuilder.create(HostgroupCfg.class));
        registerObjectBuilder(ObjectBuilder.create(ServiceCfg.class));
        registerObjectBuilder(ObjectBuilder.create(ServicegroupCfg.class));
        registerObjectBuilder(ObjectBuilder.create(ContactCfg.class));
        registerObjectBuilder(ObjectBuilder.create(ContactgroupCfg.class));
        registerObjectBuilder(ObjectBuilder.create(TimeperiodCfg.class));
        registerObjectBuilder(ObjectBuilder.create(CommandCfg.class));
    }

    private Logger logger = Logger.getLogger(NagiosConfigBuilder.class);

    private File basePath;

    private Set<File> parsedFiles = new HashSet<File>();

    private List<ConfigObject<?>> configObjects = new LinkedList<ConfigObject<?>>();

    private List<HostCfg> hosts = new LinkedList<HostCfg>();

    private List<HostgroupCfg> hostgroups = new LinkedList<HostgroupCfg>();

    private List<ServiceCfg> services = new LinkedList<ServiceCfg>();

    private List<ServicegroupCfg> servicegroups = new LinkedList<ServicegroupCfg>();

    private List<TimeperiodCfg> timeperiods = new LinkedList<TimeperiodCfg>();

    private List<CommandCfg> commands = new LinkedList<CommandCfg>();

    private List<ContactCfg> contacts = new LinkedList<ContactCfg>();

    private List<ContactgroupCfg> contactgroups = new LinkedList<ContactgroupCfg>();
    
    private Map<TemplateKey, ConfigObject<?>> templates = new LinkedHashMap<TemplateKey, ConfigObject<?>>();

    private Stack<File> toParse = new Stack<File>();

    public NagiosConfigBuilder(File basePath)
    {
        super();
        this.basePath = basePath;
    }

    // access to the parsed objects

    public File getBasePath()
    {
        return basePath;
    }

    public Set<File> getParsedFiles()
    {
        return parsedFiles;
    }

    public List<HostgroupCfg> getHostgroups()
    {
        return hostgroups;
    }

    public List<ServiceCfg> getServices()
    {
        return services;
    }

    public List<ServicegroupCfg> getServicegroups()
    {
        return servicegroups;
    }

    public List<TimeperiodCfg> getTimeperiods()
    {
        return timeperiods;
    }

    public List<CommandCfg> getCommands()
    {
        return commands;
    }

    public List<ContactCfg> getContacts()
    {
        return contacts;
    }

    public List<ContactgroupCfg> getContactgroups()
    {
        return contactgroups;
    }

    public List<ConfigObject<?>> getConfigObjects()
    {
        return configObjects;
    }

    public List<HostCfg> getHosts()
    {
        return hosts;
    }
    
    public Map<TemplateKey, ConfigObject<?>> getTemplates()
    {
        return this.templates;
    }
    
    public ConfigObject<?> getTemplate(Class<? extends ConfigObject<?>> type, String name)
    {
        return this.templates.get(new TemplateKey(type, name));
    }

    //

    public NagiosConfigBuilder includeFile(File file)
    {
        this.toParse.add(file);
        return this;
    }

    public NagiosConfigBuilder includeDir(File dir)
    {
        Stack<File> work = new Stack<File>();
        work.push(dir);
        while (!work.isEmpty())
        {
            File f = work.pop();
            if (f.isFile() && f.getName().endsWith(".cfg"))
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

    public NagiosConfigBuilder parse() throws IOException
    {
        // parse all files
        while (!this.toParse.isEmpty())
        {
            this.parseConfigFile(this.toParse.pop());
        }
        // compute the inheritance
        this.computeInheritanceGraph();
        return this;
    }

    private void parseConfigFile(File configFile) throws IOException
    {
        if (!this.parsedFiles.contains(configFile))
        {
            this.parsedFiles.add(configFile);
            NagiosConfigParser parser = new NagiosConfigParser(new FileReader(configFile));
            Directive d;
            while ((d = parser.readNext()) != null)
            {
                logger.trace("Processing directive " + d);
                // process object definitions
                if (d instanceof ObjectDefinition)
                {
                    ObjectDefinition def = (ObjectDefinition) d;
                    ObjectBuilder builder = builders.get(def.getType());
                    if (builder != null)
                    {
                        ConfigObject<?> co = builder.build(def);
                        this.configObjects.add(co);
                        // is it a template?
                        if (co.getName() != null)
                        {
                            logger.trace("Registering template " + co.getName() + " " + co);
                            this.templates.put(new TemplateKey(co.getClass(), co.getName()), co);
                        }
                        // object specific lists
                        if (co instanceof HostCfg)
                        {
                            this.hosts.add((HostCfg) co);
                        }
                        else if (co instanceof HostgroupCfg)
                        {
                            this.hostgroups.add((HostgroupCfg) co);
                        }
                        else if (co instanceof ServiceCfg)
                        {
                            this.services.add((ServiceCfg) co);
                        }
                        else if (co instanceof ServicegroupCfg)
                        {
                            this.servicegroups.add((ServicegroupCfg) co);
                        }
                        else if (co instanceof TimeperiodCfg)
                        {
                            this.timeperiods.add((TimeperiodCfg) co);
                        }
                        else if (co instanceof ContactCfg)
                        {
                            this.contacts.add((ContactCfg) co);
                        }
                        else if (co instanceof ContactgroupCfg)
                        {
                            this.contactgroups.add((ContactgroupCfg) co);
                        }
                        else if (co instanceof CommandCfg)
                        {
                            this.commands.add((CommandCfg) co);
                        }
                    }
                    else
                    {
                        logger.warn("Unsupported object " + def.getType() + " skipping");
                    }
                }
                else if (d instanceof IncludeFile)
                {
                    IncludeFile incFl = (IncludeFile) d;
                    File toInclude = new File(this.basePath, incFl.getFile());
                    logger.info("Including file: " + toInclude.getAbsolutePath());
                    this.includeFile(toInclude);
                }
                else if (d instanceof IncludeDir)
                {
                    IncludeDir incDr = (IncludeDir) d;
                    File toInclude = new File(this.basePath, incDr.getFile());
                    logger.info("Including directory: " + toInclude.getAbsolutePath());
                    this.includeDir(toInclude);
                }
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void computeInheritanceGraph()
    {
        for (ConfigObject<?> co : this.configObjects)
        {
            for (String inherits : co.getInherits())
            {
                logger.trace("The " + co + " inherits from " + inherits);
                ConfigObject<?> inherited = this.templates.get(new TemplateKey(co.getClass(), inherits));
                if (inherited != null)
                {
                    ((ConfigObject)co).addInheritedObject(inherited);
                }
                else
                {
                    logger.error("Could not find inherited object '" + inherits + "'");
                }
            }
        }
    }
    
    public static final class TemplateKey
    {
        private final Class<?> type;
        
        private final String name;
        
        public TemplateKey(Class<?> type, String name)
        {
            this.type = type;
            this.name = name;
        }
        
        public Class<?> getType()
        {
            return type;
        }
        
        public String getName()
        {
            return this.name;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((type == null) ? 0 : type.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            TemplateKey other = (TemplateKey) obj;
            if (name == null)
            {
                if (other.name != null) return false;
            }
            else if (!name.equals(other.name)) return false;
            if (type == null)
            {
                if (other.type != null) return false;
            }
            else if (!type.equals(other.type)) return false;
            return true;
        }
    }
}
