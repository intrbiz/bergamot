package com.intrbiz.bergamot.queue.key;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.UUID;

import org.junit.Test;


public class ResultKeyTest
{
    public static UUID SITE_ID = UUID.fromString("90947610-44bc-4000-8000-000000000000");
    
    public static UUID OBJECT_ID = UUID.fromString("90947610-44bc-442e-825a-06de578ea593");
    
    @Test
    public void testToSiteId()
    {
        assertThat(ResultKey.toSiteId(OBJECT_ID), is(equalTo(SITE_ID)));
    }
    
    @Test
    public void testToSiteIdIdentity()
    {
        assertThat(ResultKey.toSiteId(SITE_ID), is(equalTo(SITE_ID)));
    }
    
    @Test
    public void testActiveResultKey()
    {
        assertThat(new ActiveResultKey(SITE_ID, 0).toString(), is(equalTo(SITE_ID + ".0")));
    }
    
    @Test
    public void testPassiveResultKey()
    {
        assertThat(new PassiveResultKey(SITE_ID).toString(), is(equalTo(SITE_ID.toString())));
    }
    
    @Test
    public void testNullPassiveResultKey()
    {
        assertThat(new PassiveResultKey().toString(), is(equalTo("")));
    }
}
