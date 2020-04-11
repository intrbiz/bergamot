package com.intrbiz.bergamot.config;

import java.io.File;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import com.intrbiz.bergamot.config.validator.ValidatedBergamotConfiguration;

public class TestParseConfigTemplate
{
    private File configDir;
    
    @Before
    public void setup()
    {
        File pwd = new File(".").getAbsoluteFile().getParentFile();
        if (! "bergamot-site-config-template".equals(pwd.getName()))
        {
            configDir = new File(pwd, "bergamot-site-config-template");
        }
        configDir = new File(pwd.getAbsolutePath() + "/src/main/cfg");
    }
    
    @Test
    public void testParseConfigTemplate()
    {
        Collection<ValidatedBergamotConfiguration> validateConfigurations = new BergamotConfigReader().includeDir(this.configDir.getAbsoluteFile()).build();
        // assert the configuration is valid
        assertThat(validateConfigurations.size(), is(equalTo(1)));
        for (ValidatedBergamotConfiguration validatedConfiguration : validateConfigurations)
        {
            assertThat(validatedConfiguration, is(not(nullValue())));
            assertThat(validatedConfiguration.getReport(), is(not(nullValue())));
            
            System.out.println(validatedConfiguration.getReport().toString());
            
            assertThat(validatedConfiguration.getReport().isValid(), is(equalTo(true)));
            assertThat(validatedConfiguration.getReport().getWarnings().size(), is(equalTo(0)));
            assertThat(validatedConfiguration.getReport().getErrors().size(), is(equalTo(0)));
            
            assertThat(validatedConfiguration.getReport().getSite(), is(equalTo("bergamot.template")));
        }
    }
    
}
