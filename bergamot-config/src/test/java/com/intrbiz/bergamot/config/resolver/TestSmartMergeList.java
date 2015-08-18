package com.intrbiz.bergamot.config.resolver;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.intrbiz.bergamot.config.resolver.stratergy.SmartMergeList;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

public class TestSmartMergeList
{
    @Test
    public void testMostNull()
    {
        List<String> l = Arrays.asList("a", "b", "c");
        List<String> m = null;
        List<String> r = new SmartMergeList().resolve(m, l);
        assertThat(r, is(not(nullValue())));
        assertThat(r.isEmpty(), is(false));
        assertThat(r, contains("a", "b", "c"));
    }
    
    @Test
    public void testLeastNull()
    {
        List<String> l = null;
        List<String> m = Arrays.asList("d", "e", "f");
        List<String> r = new SmartMergeList().resolve(m, l);
        assertThat(r, is(not(nullValue())));
        assertThat(r.isEmpty(), is(false));
        assertThat(r, contains("d", "e", "f"));
    }
    
    @Test
    public void testBothNull()
    {
        List<String> l = null;
        List<String> m = null;
        List<String> r = new SmartMergeList().resolve(m, l);
        assertThat(r, is(not(nullValue())));
        assertThat(r.isEmpty(), is(true));
    }
    
    @Test
    public void testNullValueMerge()
    {
        List<String> l = Arrays.asList("a", null, "c");
        List<String> m = Arrays.asList("d", "e", null);
        List<String> r = new SmartMergeList().resolve(m, l);
        assertThat(r, is(not(nullValue())));
        assertThat(r.isEmpty(), is(false));
        assertThat(r, contains("a", "c", "d", "e"));
    }
    
    @Test
    public void testPlainMerge()
    {
        List<String> l = Arrays.asList("a", "b", "c");
        List<String> m = Arrays.asList("d", "e", "f");
        List<String> r = new SmartMergeList().resolve(m, l);
        assertThat(r, is(not(nullValue())));
        assertThat(r.isEmpty(), is(false));
        assertThat(r, contains("a", "b", "c", "d", "e", "f"));
    }
    
    @Test
    public void testUniqueMerge()
    {
        List<String> m = Arrays.asList("c", "d", "e");
        List<String> l = Arrays.asList("a", "b", "c");
        List<String> r = new SmartMergeList().resolve(m, l);
        assertThat(r, is(not(nullValue())));
        assertThat(r.isEmpty(), is(false));
        assertThat(r, contains("a", "b", "c", "d", "e"));
    }
    
    @Test
    public void testAddMerge()
    {
        List<String> l = Arrays.asList("+a", "+b", "+c");
        List<String> m = Arrays.asList("+d", "+e", "+f");
        List<String> r = new SmartMergeList().resolve(m, l);
        assertThat(r, is(not(nullValue())));
        assertThat(r.isEmpty(), is(false));
        assertThat(r, contains("a", "b", "c", "d", "e", "f"));
    }
    
    @Test
    public void testAddPlainMerge()
    {
        List<String> l = Arrays.asList("a", "+b", "c");
        List<String> m = Arrays.asList("+d", "e", "+f");
        List<String> r = new SmartMergeList().resolve(m, l);
        assertThat(r, is(not(nullValue())));
        assertThat(r.isEmpty(), is(false));
        assertThat(r, contains("a", "b", "c", "d", "e", "f"));
    }
    
    @Test
    public void testAddRemoveMerge()
    {
        List<String> l = Arrays.asList("+a", "+b", "+c");
        List<String> m = Arrays.asList("-a", "+d", "+e", "+f", "-b");
        List<String> r = new SmartMergeList().resolve(m, l);
        assertThat(r, is(not(nullValue())));
        assertThat(r.isEmpty(), is(false));
        assertThat(r, contains("c", "d", "e", "f"));
    }
    
    @Test
    public void testAddRemovePlainMerge()
    {
        List<String> l = Arrays.asList("+a", "b", "c");
        List<String> m = Arrays.asList("-a", "+d", "e", "+f", "-b");
        List<String> r = new SmartMergeList().resolve(m, l);
        assertThat(r, is(not(nullValue())));
        assertThat(r.isEmpty(), is(false));
        assertThat(r, contains("c", "d", "e", "f"));
    }
}
