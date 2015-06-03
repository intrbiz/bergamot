package com.intrbiz.bergamot.nagios.model;

import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

public class TestNagiosResult
{
    @Test
    public void testParseCheckLoad()
    {
        NagiosResult result = new NagiosResult().parseNagiosOutput("OK - load average: 0.49, 0.76, 0.85|load1=0.490;15.000;30.000;0; load5=0.760;10.000;25.000;0; load15=0.850;5.000;20.000;0;", 0, 0);
        assertThat(result, is(notNullValue()));
        assertThat(result.getResponseCode(), is(equalTo(0)));
        assertThat(result.isOk(), is(equalTo(true)));
        assertThat(result.isWarning(), is(equalTo(false)));
        assertThat(result.isCritical(), is(equalTo(false)));
        assertThat(result.isUnknown(), is(equalTo(false)));
        assertThat(result.isError(), is(equalTo(false)));
        assertThat(result.getOutput(), is(equalTo("OK - load average: 0.49, 0.76, 0.85")));
        assertThat(result.getAdditionalOutput().size(), is(equalTo(0)));
        assertThat(result.getPerfData().size(), is(equalTo(3)));
        assertThat(result.getPerfData().get(0).getLabel(), is(equalTo("load1")));
        assertThat(result.getPerfData().get(0).getValue(), is(equalTo("0.490")));
    }
    
    @Test
    public void testParseCheckDisk()
    {
        NagiosResult result = new NagiosResult().parseNagiosOutput("DISK OK - free space: / 10051 MB (49% inode=-); /home 90952 MB (40% inode=99%);| /=10181MB;16384;18432;0;20480 /home=132655MB;178885;201246;0;223607", 0, 0);
        assertThat(result, is(notNullValue()));
        assertThat(result.getResponseCode(), is(equalTo(0)));
        assertThat(result.isOk(), is(equalTo(true)));
        assertThat(result.isWarning(), is(equalTo(false)));
        assertThat(result.isCritical(), is(equalTo(false)));
        assertThat(result.isUnknown(), is(equalTo(false)));
        assertThat(result.isError(), is(equalTo(false)));
        assertThat(result.getOutput(), is(equalTo("DISK OK - free space: / 10051 MB (49% inode=-); /home 90952 MB (40% inode=99%);")));
        assertThat(result.getAdditionalOutput().size(), is(equalTo(0)));
        assertThat(result.getPerfData().size(), is(equalTo(2)));
        assertThat(result.getPerfData().get(0).getLabel(), is(equalTo("/")));
        assertThat(result.getPerfData().get(0).getValue(), is(equalTo("10181")));
        assertThat(result.getPerfData().get(0).getUnit(), is(equalTo("MB")));
    }
}
