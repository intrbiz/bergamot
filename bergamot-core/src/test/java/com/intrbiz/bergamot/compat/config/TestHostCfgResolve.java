package com.intrbiz.bergamot.compat.config;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.Test;

import com.intrbiz.bergamot.compat.config.model.HostCfg;


public class TestHostCfgResolve
{
    private HostCfg parent;
    
    private HostCfg child;
    
    @Before
    public void setup()
    {
        this.parent = new HostCfg();
        this.parent.setRegister(false);
        this.parent.setName("generic-host");
        this.parent.setCheckInterval(300L);
        this.parent.setRetryInterval(60L);
        this.parent.setMaxCheckAttempts(3);
        //
        this.child = new HostCfg();
        this.child.addInherit("generic-host");
        this.child.addInheritedObject(this.parent);
        this.child.setHostName("localhost");
        this.child.setAddress("127.0.0.1");
        this.child.setDisplayName("Localhost");
        this.child.setAlias("Localhost");
    }

    @Test
    public void testMaxCheckAttempts()
    {
        assertThat(this.child.resolveMaxCheckAttempts(), is(equalTo(3)));
    }
    
    @Test
    public void testCheckInterval()
    {
        assertThat(this.child.resolveCheckInterval(), is(equalTo(300L)));
    }
    
    @Test
    public void testRetryInterval()
    {
        assertThat(this.child.resolveRetryInterval(), is(equalTo(60L)));
    }

}
