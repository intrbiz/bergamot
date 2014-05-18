package com.intrbiz.bergamot;

import java.io.File;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.intrbiz.bergamot.compat.config.builder.NagiosConfigBuilder;
import com.intrbiz.bergamot.config.BergamotConfigWriter;
import com.intrbiz.bergamot.config.NagiosConfigConverter;
import com.intrbiz.bergamot.config.model.BergamotCfg;

public class NagiosToBergamot
{
    public static void main(String[] args) throws Exception
    {
        // setup logging
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        // convert
        if (args.length != 1)
        {
            usage();
        }
        else
        {
            File dir = new File(args[0]);
            if (dir.isDirectory())
            {
                convert(dir);
            }
            else
            {
                System.err.println("Cannot convert, the path " + args[0] + " is not a directory");
            }
        }
    }
    
    public static void usage()
    {
        System.err.println("Nagios to Bergamot Config Converter Usage:");
        System.err.println(" nagios2bergamot /path/to/configuration/directory");
        
        System.exit(1);
    }
    
    public static void convert(File nagiosCfg) throws Exception
    {
        // convert
        NagiosConfigBuilder nagios = new NagiosConfigBuilder(nagiosCfg).includeDir(nagiosCfg).parse();
        BergamotCfg cfg = new NagiosConfigConverter().baseDir(nagiosCfg).site("bergamot.local").nagiosConfig(nagios).convert();
        // validate
        cfg.computeInheritenance();
        cfg.validate();
        // write
        new BergamotConfigWriter().keepFileStructure(true).config(cfg).write();
        System.exit(0);
    }
}
