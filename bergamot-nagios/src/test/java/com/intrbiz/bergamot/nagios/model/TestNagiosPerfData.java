package com.intrbiz.bergamot.nagios.model;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.nio.CharBuffer;
import java.util.List;

import org.junit.Test;

public class TestNagiosPerfData
{

    @Test
    public void testReadUnquotedLabel() throws Exception
    {
        String label = NagiosPerfData.readLabel(CharBuffer.wrap("load1=0.490;15.000;30.000;0;".toCharArray()));
        assertThat(label, is(equalTo("load1")));
    }
    
    @Test
    public void testReadQuotedLabel() throws Exception
    {
        String label = NagiosPerfData.readLabel(CharBuffer.wrap("'chris'' load1'=0.490;15.000;30.000;0;".toCharArray()));
        assertThat(label, is(equalTo("chris' load1")));
    }
    
    @Test
    public void testParsePerfData() throws Exception
    {
        List<NagiosPerfData> perfData = NagiosPerfData.parsePerfData("load1=0.490;15.000;30.000;0; load5=0.760;10.000;25.000;0; load15=0.850;5.000;20.000;0;");
        assertThat(perfData.size(), is(equalTo(3)));
    }

}
