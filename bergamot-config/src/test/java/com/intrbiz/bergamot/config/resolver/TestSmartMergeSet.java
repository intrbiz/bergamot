package com.intrbiz.bergamot.config.resolver;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;

import com.intrbiz.bergamot.config.resolver.stratergy.SmartMergeSet;

public class TestSmartMergeSet
{
    @Test
    public void testMostNull()
    {
        Set<String> l = new LinkedHashSet<String>(Arrays.asList("a", "b", "c"));
        Set<String> m = null;
        Set<String> r = new SmartMergeSet().resolve(m, l);
        assertThat(r, is(not(nullValue())));
        assertThat(r.isEmpty(), is(false));
        assertThat(r, contains("a", "b", "c"));
    }
    
    @Test
    public void testLeastNull()
    {
        Set<String> l = null;
        Set<String> m = new LinkedHashSet<String>(Arrays.asList("d", "e", "f"));
        Set<String> r = new SmartMergeSet().resolve(m, l);
        assertThat(r, is(not(nullValue())));
        assertThat(r.isEmpty(), is(false));
        assertThat(r, contains("d", "e", "f"));
    }
    
    @Test
    public void testBothNull()
    {
        Set<String> l = null;
        Set<String> m = null;
        Set<String> r = new SmartMergeSet().resolve(m, l);
        assertThat(r, is(not(nullValue())));
        assertThat(r.isEmpty(), is(true));
    }
    
    @Test
    public void testNullValueMerge()
    {
        Set<String> l = new LinkedHashSet<String>(Arrays.asList("a", null, "c"));
        Set<String> m = new LinkedHashSet<String>(Arrays.asList("d", "e", null));
        Set<String> r = new SmartMergeSet().resolve(m, l);
        assertThat(r, is(not(nullValue())));
        assertThat(r.isEmpty(), is(false));
        assertThat(r, contains("a", "c", "d", "e"));
    }
    
    @Test
    public void testPlainMerge()
    {
        Set<String> l = new LinkedHashSet<String>(Arrays.asList("a", "b", "c"));
        Set<String> m = new LinkedHashSet<String>(Arrays.asList("d", "e", "f"));
        Set<String> r = new SmartMergeSet().resolve(m, l);
        assertThat(r, is(not(nullValue())));
        assertThat(r.isEmpty(), is(false));
        assertThat(r, contains("a", "b", "c", "d", "e", "f"));
    }
    
    @Test
    public void testUniqueMerge()
    {
        Set<String> m = new LinkedHashSet<String>(Arrays.asList("c", "d", "e"));
        Set<String> l = new LinkedHashSet<String>(Arrays.asList("a", "b", "c"));
        Set<String> r = new SmartMergeSet().resolve(m, l);
        assertThat(r, is(not(nullValue())));
        assertThat(r.isEmpty(), is(false));
        assertThat(r, contains("a", "b", "c", "d", "e"));
    }
    
    @Test
    public void testAddMerge()
    {
        Set<String> l = new LinkedHashSet<String>(Arrays.asList("+a", "+b", "+c"));
        Set<String> m = new LinkedHashSet<String>(Arrays.asList("+d", "+e", "+f"));
        Set<String> r = new SmartMergeSet().resolve(m, l);
        assertThat(r, is(not(nullValue())));
        assertThat(r.isEmpty(), is(false));
        assertThat(r, contains("a", "b", "c", "d", "e", "f"));
    }
    
    @Test
    public void testAddPlainMerge()
    {
        Set<String> l = new LinkedHashSet<String>(Arrays.asList("a", "+b", "c"));
        Set<String> m = new LinkedHashSet<String>(Arrays.asList("+d", "e", "+f"));
        Set<String> r = new SmartMergeSet().resolve(m, l);
        assertThat(r, is(not(nullValue())));
        assertThat(r.isEmpty(), is(false));
        assertThat(r, contains("a", "b", "c", "d", "e", "f"));
    }
    
    @Test
    public void testAddRemoveMerge()
    {
        Set<String> l = new LinkedHashSet<String>(Arrays.asList("+a", "+b", "+c"));
        Set<String> m = new LinkedHashSet<String>(Arrays.asList("-a", "+d", "+e", "+f", "-b"));
        Set<String> r = new SmartMergeSet().resolve(m, l);
        assertThat(r, is(not(nullValue())));
        assertThat(r.isEmpty(), is(false));
        assertThat(r, contains("c", "d", "e", "f"));
    }
    
    @Test
    public void testAddRemovePlainMerge()
    {
        Set<String> l = new LinkedHashSet<String>(Arrays.asList("+a", "b", "c"));
        Set<String> m = new LinkedHashSet<String>(Arrays.asList("-a", "+d", "e", "+f", "-b"));
        Set<String> r = new SmartMergeSet().resolve(m, l);
        assertThat(r, is(not(nullValue())));
        assertThat(r.isEmpty(), is(false));
        assertThat(r, contains("c", "d", "e", "f"));
    }
}
