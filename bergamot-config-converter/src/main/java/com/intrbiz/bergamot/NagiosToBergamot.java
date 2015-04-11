package com.intrbiz.bergamot;

import java.io.File;

import com.intrbiz.bergamot.compat.config.builder.NagiosConfigBuilder;
import com.intrbiz.bergamot.config.BergamotConfigWriter;
import com.intrbiz.bergamot.config.NagiosConfigConverter;
import com.intrbiz.bergamot.config.model.BergamotCfg;
import com.intrbiz.bergamot.config.validator.ValidatedBergamotConfiguration;

public class NagiosToBergamot
{    
    public static void convert(File nagiosCfg) throws Exception
    {
        // convert
        NagiosConfigBuilder nagios = new NagiosConfigBuilder(nagiosCfg).includeDir(nagiosCfg).parse();
        BergamotCfg cfg = new NagiosConfigConverter().baseDir(nagiosCfg).site("bergamot.local").nagiosConfig(nagios).convert();
        // validate
        ValidatedBergamotConfiguration validated = cfg.validate();
        System.out.println(validated.getReport().toString());
        // write
        new BergamotConfigWriter().baseDir(nagiosCfg).keepFileStructure(true).config(cfg).write();
    }
    
    public static void main(String[] args) throws Exception
    {
        NagiosToBergamot.convert(new File(args[0]));
    }
}
