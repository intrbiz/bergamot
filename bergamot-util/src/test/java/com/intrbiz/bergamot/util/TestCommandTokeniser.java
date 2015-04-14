package com.intrbiz.bergamot.util;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.intrbiz.bergamot.util.CommandTokeniser;

public class TestCommandTokeniser
{
    @Test
    public void testSingleToken() throws Exception
    {
        List<String> command = CommandTokeniser.tokeniseCommandLine("test");
        assertThat(command.size(), is(equalTo(1)));
        assertThat(command.get(0), is(equalTo("test")));
    }
    
    @Test
    public void testDoubleQuotedSingleToken() throws Exception
    {
        List<String> command = CommandTokeniser.tokeniseCommandLine("\"Testing 123\"");
        assertThat(command.size(), is(equalTo(1)));
        assertThat(command.get(0), is(equalTo("\"Testing 123\"")));
    }
    
    @Test
    public void testSingleQuotedSingleToken() throws Exception
    {
        List<String> command = CommandTokeniser.tokeniseCommandLine("'Testing 123'");
        assertThat(command.size(), is(equalTo(1)));
        assertThat(command.get(0), is(equalTo("'Testing 123'")));
    }
    
    @Test
    public void testSingleQuotedSingleTokenWithEscapes() throws Exception
    {
        List<String> command = CommandTokeniser.tokeniseCommandLine("'Testing \\' \\\" 123'");
        assertThat(command.size(), is(equalTo(1)));
        assertThat(command.get(0), is(equalTo("'Testing \\' \\\" 123'")));
    }
    
    @Test
    public void testDoubleQuotedSingleTokenWithEscapes() throws Exception
    {
        List<String> command = CommandTokeniser.tokeniseCommandLine("\"Testing \\' \\\" 123\"");
        assertThat(command.size(), is(equalTo(1)));
        assertThat(command.get(0), is(equalTo("\"Testing \\' \\\" 123\"")));
    }
    
    @Test
    public void testMixedQuotesSingleToken() throws Exception
    {
        try
        {
            CommandTokeniser.tokeniseCommandLine("\"Broken test'");
            Assert.assertTrue("Broken quoting failed to raise exception", false);
        }
        catch (IOException e)
        {
            Assert.assertTrue("Broken quoting caused exception", true);
        }
    }
    
    @Test
    public void testOpenQuote() throws Exception
    {
        try
        {
            CommandTokeniser.tokeniseCommandLine("\"Broken test");
            Assert.assertTrue("Broken quoting failed to raise exception", false);
        }
        catch (IOException e)
        {
            Assert.assertTrue("Broken quoting caused exception", true);
        }
    }
    
    @Test
    public void testMultiQuotedTokens() throws Exception
    {
        List<String> command = CommandTokeniser.tokeniseCommandLine("\"Here\" 'is' some 'quoted' \"tokens\" to parse");
        assertThat(command.size(), is(equalTo(7)));
        assertThat(command.get(0), is(equalTo("\"Here\"")));
        assertThat(command.get(1), is(equalTo("'is'")));
        assertThat(command.get(2), is(equalTo("some")));
        assertThat(command.get(3), is(equalTo("'quoted'")));
        assertThat(command.get(4), is(equalTo("\"tokens\"")));
        assertThat(command.get(5), is(equalTo("to")));
        assertThat(command.get(6), is(equalTo("parse")));
    }
    
    @Test
    public void testSimpleCommand() throws Exception
    {
        List<String> command = CommandTokeniser.tokeniseCommandLine("/usr/lib/nagios/plugins/check_dummy 0 \"Testing 123\"");
        assertThat(command.size(), is(equalTo(3)));
        assertThat(command.get(0), is(equalTo("/usr/lib/nagios/plugins/check_dummy")));
        assertThat(command.get(1), is(equalTo("0")));
        assertThat(command.get(2), is(equalTo("\"Testing 123\"")));
    }
}
