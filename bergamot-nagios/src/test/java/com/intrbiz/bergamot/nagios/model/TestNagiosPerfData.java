package com.intrbiz.bergamot.nagios.model;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.nio.CharBuffer;
import java.util.List;

import org.junit.Test;

import com.intrbiz.gerald.polyakov.Reading;
import com.intrbiz.gerald.polyakov.gauge.DoubleGaugeReading;

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
    
    @Test
    public void testParsePingPerfData() throws Exception
    {
        List<NagiosPerfData> perfData = NagiosPerfData.parsePerfData("rta=15.289000ms;3000.000000;5000.000000;0.000000 pl=0%;80;100;0");
        assertThat(perfData.size(), is(equalTo(2)));
        // rta metric
        NagiosPerfData rta = perfData.get(0);
        assertThat(rta, is(notNullValue()));
        Reading rrta = rta.toReading();
        assertThat(rrta, is(notNullValue()));
        assertThat(rrta, is(instanceOf(DoubleGaugeReading.class)));
        assertThat(rrta.getName(), is(equalTo("rta")));
        assertThat(rrta.getUnit(), is(equalTo("ms")));
        assertThat(((DoubleGaugeReading)rrta).getValue(), is(equalTo(15.289000D)));
        assertThat(((DoubleGaugeReading)rrta).getWarning(), is(equalTo(3000.000000D)));
        assertThat(((DoubleGaugeReading)rrta).getCritical(), is(equalTo(5000.000000D)));
        assertThat(((DoubleGaugeReading)rrta).getMin(), is(equalTo(0D)));
        // pl metric
        NagiosPerfData pl = perfData.get(1);
        assertThat(pl, is(notNullValue()));
        Reading rpl = pl.toReading();
        assertThat(rpl, is(notNullValue()));
        assertThat(rpl, is(instanceOf(DoubleGaugeReading.class)));
        assertThat(rpl.getName(), is(equalTo("pl")));
        assertThat(rpl.getUnit(), is(equalTo("%")));
        assertThat(((DoubleGaugeReading)rpl).getValue(), is(equalTo(0D)));
        assertThat(((DoubleGaugeReading)rpl).getWarning(), is(equalTo(80D)));
        assertThat(((DoubleGaugeReading)rpl).getCritical(), is(equalTo(100D)));
        assertThat(((DoubleGaugeReading)rpl).getMin(), is(equalTo(0D)));
    }
    
    

}
